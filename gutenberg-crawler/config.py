import os
from dotenv import load_dotenv

# 이 스크립트 파일의 위치를 기준으로 .env 파일을 찾습니다.
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path=dotenv_path)

# --- DATABASE CONFIGURATION (MySQL) ---
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_NAME = os.getenv("DB_NAME")
DB_PORT = int(os.getenv("DB_PORT", 3306))

# --- FIREBASE CONFIGURATION ---
# firebase-admin 초기화를 위해 서비스 계정 키 파일의 경로를 환경 변수에서 읽어옵니다.
# 예: GOOGLE_APPLICATION_CREDENTIALS="C:/path/to/your/serviceAccountKey.json"
# 해당 환경 변수는 시스템에 직접 설정하거나 .env 파일에 추가할 수 있습니다.
# firebase-admin.initialize_app() 호출 시 자동으로 이 변수를 사용합니다.

# Firebase Storage 버킷 이름을 .env에서 읽어옵니다.
FIREBASE_STORAGE_BUCKET = os.getenv("FIREBASE_STORAGE_BUCKET") # e.g., 'your-project-id.appspot.com'


# --- CRAWLER CONFIGURATION ---
# Project Gutenberg Top 100 URL
TOP_100_URL = "https://www.gutenberg.org/browse/scores/top"

# --- DIRECTORY CONFIGURATION ---
# 이 파일의 위치를 기준으로 절대 경로를 구성합니다.
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
EBOOKS_DIR = os.path.join(BASE_DIR, "ebooks")
METADATA_DIR = os.path.join(BASE_DIR, "metadata")
COVERS_DIR = os.path.join(BASE_DIR, "covers")
PROCESSED_IDS_FILE = os.path.join(BASE_DIR, "processed_ids.txt")
LOG_FILE = os.path.join(BASE_DIR, "crawler.log")

# --- TRANSLATION CONFIGURATION ---
# Google Cloud Translation API 사용 시 프로젝트 ID
GOOGLE_PROJECT_ID = os.getenv("GOOGLE_PROJECT_ID")
