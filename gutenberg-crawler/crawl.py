import requests
from bs4 import BeautifulSoup
import json
import os
import time
import logging
from urllib.parse import urljoin

# 설정 및 경로 정보 로드
import config

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(config.LOG_FILE, encoding='utf-8'),
        logging.StreamHandler()
    ]
)

def get_processed_ids():
    """처리된 ID 목록을 파일에서 읽어와 set으로 반환한다."""
    if not os.path.exists(config.PROCESSED_IDS_FILE):
        return set()
    with open(config.PROCESSED_IDS_FILE, 'r', encoding='utf-8') as f:
        return set(line.strip() for line in f)

def add_processed_id(book_id):
    """처리된 ID를 파일에 추가한다."""
    with open(config.PROCESSED_IDS_FILE, 'a', encoding='utf-8') as f:
        f.write(f"{book_id}\n")

def download_file(url, save_path):
    """지정된 URL에서 파일을 다운로드하여 주어진 경로에 저장한다."""
    try:
        response = requests.get(url, stream=True, timeout=30)
        response.raise_for_status()
        with open(save_path, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)
        logging.info(f"파일 다운로드 완료: {save_path}")
        return True
    except requests.exceptions.RequestException as e:
        logging.error(f"파일 다운로드 실패 ({url}): {e}")
        return False

def get_book_details(book_id, book_page_url):
    """개별 도서 페이지에서 상세 정보, 커버, EPUB 파일을 수집하고 저장한다."""
    try:
        response = requests.get(book_page_url, timeout=15)
        response.raise_for_status()
        soup = BeautifulSoup(response.content, 'html.parser')

        metadata = {'id': book_id, 'url': book_page_url}

        # 1. 메타데이터 추출 (Title, Author 등)
        details_table = soup.find('table', {'class': 'bibrec'})
        if details_table:
            for row in details_table.find_all('tr'):
                header = row.find('th')
                value = row.find('td')
                if header and value:
                    key = header.text.strip().lower().replace(' ', '_')
                    if 'downloads' not in key and 'post' not in key:
                        metadata[key] = value.text.strip()

        # 2. 커버 이미지 URL 추출 및 다운로드
        cover_img = soup.find('img', {'class': 'cover-art'})
        if cover_img and cover_img.has_attr('src'):
            cover_url = urljoin(book_page_url, cover_img['src'])
            cover_filename = f"{book_id}.jpg"
            cover_save_path = os.path.join(config.COVERS_DIR, cover_filename)
            if download_file(cover_url, cover_save_path):
                metadata['cover_image_path'] = cover_save_path

        # 3. EPUB 파일 URL 추출 및 다운로드
        epub_link = soup.find('a', string=lambda t: t and 'epub (with images)' in t.lower())
        if not epub_link:
            epub_link = soup.find('a', string=lambda t: t and 'epub' in t.lower())

        if epub_link and epub_link.has_attr('href'):
            epub_url = urljoin(book_page_url, epub_link['href'])
            epub_filename = f"{book_id}.epub"
            epub_save_path = os.path.join(config.EBOOKS_DIR, epub_filename)
            if download_file(epub_url, epub_save_path):
                metadata['epub_file_path'] = epub_save_path

        # 4. 메타데이터를 JSON 파일로 저장
        meta_filename = f"{book_id}.json"
        meta_save_path = os.path.join(config.METADATA_DIR, meta_filename)
        with open(meta_save_path, 'w', encoding='utf-8') as f:
            json.dump(metadata, f, ensure_ascii=False, indent=4)
        logging.info(f"메타데이터 저장 완료: {meta_save_path}")
        return True

    except Exception as e:
        logging.error(f"[{book_id}] 상세 정보 처리 중 오류: {e}", exc_info=True)
        return False

def crawl():
    """Project Gutenberg의 Top 100 목록을 크롤링하여 데이터를 수집한다."""
    logging.info("크롤링을 시작합니다.")
    processed_ids = get_processed_ids()

    try:
        response = requests.get(config.TOP_100_URL, timeout=15)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        logging.error(f"Top 100 페이지에 접근할 수 없습니다: {e}")
        return

    soup = BeautifulSoup(response.content, 'html.parser')
    top_list_header = soup.find('h2', string=lambda text: text and 'Top 100' in text)
    if not top_list_header:
        logging.error("Top 100 헤더를 찾을 수 없습니다. HTML 구조 변경이 의심됩니다.")
        return

    ebook_list = top_list_header.find_next_sibling('ol')
    if not ebook_list:
        logging.error("Top 100 도서 목록(<ol>)을 찾을 수 없습니다. HTML 구조가 변경되었을 수 있습니다.")
        return

    book_links = ebook_list.find_all('a')
    logging.info(f"총 {len(book_links)}개의 도서 링크를 발견했습니다.")

    for link in book_links:
        time.sleep(1)
        try:
            relative_path = link['href']
            book_id = relative_path.split('/')[-1]

            if book_id in processed_ids:
                logging.info(f"[{book_id}] 이미 처리된 도서입니다. 건너뜁니다.")
                continue

            book_page_url = urljoin(config.TOP_100_URL, relative_path)
            logging.info(f"[{book_id}] 도서 페이지 처리 시작: {book_page_url}")

            if get_book_details(book_id, book_page_url):
                add_processed_id(book_id)
                logging.info(f"[{book_id}] 처리가 성공적으로 완료되었습니다.")
            else:
                logging.warning(f"[{book_id}] 처리 중 오류가 발생하여 건너뜁니다.")

        except Exception as e:
            logging.error(f"도서 처리 중 오류 발생 (링크: {link.get('href', 'N/A')}): {e}", exc_info=True)

    logging.info("크롤링 작업이 모두 완료되었습니다.")

if __name__ == "__main__":
    os.makedirs(config.METADATA_DIR, exist_ok=True)
    os.makedirs(config.EBOOKS_DIR, exist_ok=True)
    os.makedirs(config.COVERS_DIR, exist_ok=True)
    crawl()