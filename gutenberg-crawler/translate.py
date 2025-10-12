import os
import json
import logging
import time
from google.cloud import translate_v2 as translate
import google.auth

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

def translate_text(client, text, target_language='ko'):
    """주어진 텍스트를 대상 언어로 번역한다."""
    if not text or not isinstance(text, str):
        return ""
    try:
        # API 요청 속도 조절
        time.sleep(0.1)
        result = client.translate(text, target_language=target_language)
        return result['translatedText']
    except Exception as e:
        logging.error(f"번역 API 호출 중 오류 발생: {e}")
        # 번역 실패 시 원본 텍스트의 일부를 반환하여 오류 인지
        return f"Translation Error: {text[:50]}..."

def translate_metadata_files():
    """metadata 디렉토리의 모든 JSON 파일 내용을 번역한다."""
    logging.info("메타데이터 번역을 시작합니다.")

    try:
        # GOOGLE_APPLICATION_CREDENTIALS 환경변수를 통해 인증 정보 자동 로드
        credentials, project = google.auth.default()
        if not project and config.GOOGLE_PROJECT_ID:
            project = config.GOOGLE_PROJECT_ID
        logging.info(f"Google Cloud 인증 성공. 프로젝트 ID: {project or 'Not set'}")
    except google.auth.exceptions.DefaultCredentialsError:
        logging.error("Google Cloud 인증 실패. GOOGLE_APPLICATION_CREDENTIALS 환경 변수가 올바르게 설정되었는지 확인하세요.")
        return

    translate_client = translate.Client()

    json_files = [f for f in os.listdir(config.METADATA_DIR) if f.endswith('.json')]
    logging.info(f"총 {len(json_files)}개의 메타데이터 파일을 발견했습니다.")

    for filename in json_files:
        file_path = os.path.join(config.METADATA_DIR, filename)

        try:
            with open(file_path, 'r+', encoding='utf-8') as f:
                data = json.load(f)

                book_id = data.get('id', os.path.splitext(filename)[0])

                # 이미 번역된 필드가 있는지 확인하여 중복 작업 방지
                if 'title_kor' in data:
                    logging.info(f"[{book_id}] 이미 번역된 파일입니다. 건너뜁니다.")
                    continue

                logging.info(f"[{book_id}] 파일 번역 중: {filename}")

                # 번역할 필드 목록
                fields_to_translate = ['title', 'author', 'subjects']

                for field in fields_to_translate:
                    if field in data:
                        original_text = data[field]
                        translated_text = translate_text(translate_client, original_text)
                        data[f"{field}_kor"] = translated_text

                # 파일의 처음으로 커서를 이동하여 수정된 내용으로 덮어쓰기
                f.seek(0)
                json.dump(data, f, ensure_ascii=False, indent=4)
                f.truncate() # 파일의 나머지 부분을 삭제 (내용이 줄어들 경우 대비)
                logging.info(f"[{book_id}] 파일 번역 완료 및 저장.")

        except Exception as e:
            logging.error(f"{filename} 파일 처리 중 오류 발생: {e}", exc_info=True)

    logging.info("메타데이터 번역 작업이 모두 완료되었습니다.")

if __name__ == "__main__":
    translate_metadata_files()
