import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin

TOP_100_URL = "https://www.gutenberg.org/browse/scores/top"

def test_single_book_crawl():
    """
    Gutenberg Top 100 목록의 첫 번째 책을 대상으로 크롤링 로직을 테스트하고
    추출된 데이터를 화면에 출력합니다.
    """
    print(f"1. Top 100 페이지에 접속합니다: {TOP_100_URL}")
    try:
        response = requests.get(TOP_100_URL, timeout=15)
        response.raise_for_status()
        print("페이지 접속 성공.")
    except requests.exceptions.RequestException as e:
        print(f"페이지 접속 실패: {e}")
        return

    soup = BeautifulSoup(response.content, 'html.parser')

    # 2. 첫 번째 도서 링크를 찾습니다.
    top_list_header = soup.find('h2', string=lambda text: text and 'Top 100' in text)
    if not top_list_header:
        print("Top 100 헤더를 찾을 수 없습니다.")
        return

    ebook_list = top_list_header.find_next_sibling('ol')
    if not ebook_list:
        print("Top 100 도서 목록(<ol>)을 찾을 수 없습니다.")
        return

    first_book_link = ebook_list.find('a')
    if not first_book_link or not first_book_link.has_attr('href'):
        print("첫 번째 도서 링크를 찾을 수 없습니다.")
        return

    book_page_url = urljoin(TOP_100_URL, first_book_link['href'])
    book_id = first_book_link['href'].split('/')[-1]
    print(f"\n2. 테스트 대상 도서 페이지: [{book_id}] {book_page_url}")

    # 3. 도서 상세 페이지 크롤링
    try:
        response = requests.get(book_page_url, timeout=15)
        response.raise_for_status()
        print("상세 페이지 접속 성공.")
    except requests.exceptions.RequestException as e:
        print(f"상세 페이지 접속 실패: {e}")
        return

    book_soup = BeautifulSoup(response.content, 'html.parser')

    print("\n--- 데이터 추출 결과 ---")

    # 4. 메타데이터 추출
    print("\n[메타데이터]")
    metadata = {}
    details_table = book_soup.find('table', {'class': 'bibrec'})
    if details_table:
        for row in details_table.find_all('tr'):
            header = row.find('th')
            value = row.find('td')
            if header and value:
                key = header.text.strip()
                val = value.text.strip()
                metadata[key] = val
                print(f"  - {key}: {val}")
    else:
        print("  - 메타데이터 테이블('bibrec')을 찾을 수 없습니다.")

    # 5. 커버 이미지 URL 추출
    print("\n[커버 이미지]")
    cover_img = book_soup.find('img', {'class': 'cover-art'})
    if cover_img and cover_img.has_attr('src'):
        cover_url = urljoin(book_page_url, cover_img['src'])
        print(f"  - URL: {cover_url}")
    else:
        print("  - 커버 이미지를 찾을 수 없습니다.")

    # 6. EPUB 파일 URL 추출
    print("\n[EPUB 파일]")
    epub_link = book_soup.find('a', string=lambda t: t and 'epub (with images)' in t.lower())
    if not epub_link:
        epub_link = book_soup.find('a', string=lambda t: t and 'epub' in t.lower())

    if epub_link and epub_link.has_attr('href'):
        epub_url = urljoin(book_page_url, epub_link['href'])
        print(f"  - URL: {epub_url}")
    else:
        print("  - EPUB 링크를 찾을 수 없습니다.")

    print("\n--- 테스트 종료 ---")

if __name__ == "__main__":
    test_single_book_crawl()