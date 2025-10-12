import os
import json
import logging
import pymysql
import firebase_admin
from firebase_admin import credentials, storage

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

# --- DATABASE SCHEMA EXAMPLE ---
# CREATE TABLE books (
#     id INT PRIMARY KEY,
#     title VARCHAR(512),
#     author VARCHAR(512),
#     language VARCHAR(50),
#     subjects TEXT,
#     title_kor VARCHAR(512),
#     author_kor VARCHAR(512),
#     subjects_kor TEXT,
#     cover_url VARCHAR(1024),
#     epub_url VARCHAR(1024),
#     gutenberg_url VARCHAR(1024),
#     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
# );

def initialize_firebase():
    """Firebase Admin SDK를 초기화한다."""
    try:
        # GOOGLE_APPLICATION_CREDENTIALS 환경 변수에서 인증 정보 자동 로드
        cred = credentials.ApplicationDefault()
        firebase_admin.initialize_app(cred, {
            'storageBucket': config.FIREBASE_STORAGE_BUCKET
        })
        logging.info("Firebase 인증 및 초기화 성공.")
        return True
    except Exception as e:
        logging.error(f"Firebase 초기화 실패: {e}", exc_info=True)
        logging.error("GOOGLE_APPLICATION_CREDENTIALS 환경 변수와 Storage 버킷 이름이 올바른지 확인하세요.")
        return False

def upload_to_firebase(bucket, source_path, destination_blob_name):
    """로컬 파일을 Firebase Storage에 업로드하고 public URL을 반환한다."""
    if not os.path.exists(source_path):
        logging.warning(f"업로드할 파일 없음: {source_path}")
        return None
    try:
        blob = bucket.blob(destination_blob_name)
        blob.upload_from_filename(source_path)
        blob.make_public()
        logging.info(f"Firebase Storage 업로드 성공: {destination_blob_name}")
        return blob.public_url
    except Exception as e:
        logging.error(f"Firebase 업로드 실패 ({source_path}): {e}", exc_info=True)
        return None

def update_database_and_storage():
    """로컬 데이터를 DB와 Storage에 업데이트한다."""
    logging.info("데이터베이스 및 스토리지 업데이트를 시작합니다.")

    if not initialize_firebase():
        return

    db_conn = None
    try:
        db_conn = pymysql.connect(
            host=config.DB_HOST,
            user=config.DB_USER,
            password=config.DB_PASSWORD,
            database=config.DB_NAME,
            port=config.DB_PORT,
            charset='utf8mb4'
        )
        cursor = db_conn.cursor()
        logging.info("MySQL 데이터베이스 연결 성공.")

        bucket = storage.bucket()
        json_files = [f for f in os.listdir(config.METADATA_DIR) if f.endswith('.json')]

        for filename in json_files:
            file_path = os.path.join(config.METADATA_DIR, filename)
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            book_id = data.get('id')
            if not book_id:
                continue

            # DB에 이미 존재하는지 확인
            cursor.execute("SELECT id FROM books WHERE id = %s", (book_id,))
            if cursor.fetchone():
                logging.info(f"[{book_id}] 이미 DB에 존재합니다. 건너뜁니다.")
                continue

            logging.info(f"[{book_id}] 신규 데이터 처리 시작.")

            # 파일 업로드
            cover_url = upload_to_firebase(bucket, data.get('cover_image_path', ''), f"covers/{book_id}.jpg")
            epub_url = upload_to_firebase(bucket, data.get('epub_file_path', ''), f"ebooks/{book_id}.epub")

            # DB에 저장할 데이터 준비
            insert_data = {
                'id': book_id,
                'title': data.get('title'),
                'author': data.get('author'),
                'language': data.get('language'),
                'subjects': data.get('subjects'),
                'title_kor': data.get('title_kor'),
                'author_kor': data.get('author_kor'),
                'subjects_kor': data.get('subjects_kor'),
                'cover_url': cover_url,
                'epub_url': epub_url,
                'gutenberg_url': data.get('url')
            }

            # SQL INSERT 실행
            cols = ', '.join(insert_data.keys())
            placeholders = ', '.join(['%s'] * len(insert_data))
            sql = f"INSERT INTO books ({cols}) VALUES ({placeholders})"
            cursor.execute(sql, tuple(insert_data.values()))
            db_conn.commit()
            logging.info(f"[{book_id}] DB에 성공적으로 추가되었습니다.")

    except pymysql.MySQLError as e:
        logging.error(f"데이터베이스 작업 중 오류 발생: {e}", exc_info=True)
        if db_conn: db_conn.rollback()
    except Exception as e:
        logging.error(f"업데이트 프로세스 중 오류 발생: {e}", exc_info=True)
    finally:
        if db_conn:
            db_conn.close()
            logging.info("데이터베이스 연결을 닫습니다.")

    logging.info("업데이트 작업이 모두 완료되었습니다.")

if __name__ == "__main__":
    update_database_and_storage()
