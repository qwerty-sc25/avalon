# 📚 Project Gutenberg Top 100 E-books 크롤링 및 데이터베이스화 계획

---

## 🎯 1. 프로젝트 목표

Project Gutenberg의 'Top 100' 목록에 있는 전자책들의 메타데이터, 커버 이미지, EPUB 파일을 수집하여 한국어로 번역하고, 이를 MySQL 데이터베이스와 Firebase Storage에 안정적으로 저장 및 관리하는 자동화 시스템을 구축한다.

---

## ✅ 2. 사전 확인 사항: `robots.txt` 준수

- **URL**: `https://www.gutenberg.org/robots.txt`
- **준수 계획**:
  - `Disallow: /ebooks/search` 규칙에 따라 검색 페이지는 크롤링하지 않는다.
  - 대상 URL(`.../browse/scores/top`)은 크롤링이 허용됨.
  - 서버 부하를 최소화하기 위해 요청 간 적절한 `sleep` 혹은 동시 요청 수 제한을 설정한다.

---

## 🛠️ 3. 작업 단계별 명세

### Phase 1: 데이터 수집 및 로컬 저장 (Crawling)

- **담당 스크립트**: `crawl.py`
- **핵심 기능**: Top 100 도서의 상세 정보, 커버 이미지, EPUB 파일을 로컬에 저장한다.

**실행 순서:**

1. `https://www.gutenberg.org/browse/scores/top` 페이지에서 도서 상세 페이지 URL 목록 수집.
2. 각 상세 페이지에 접속하여 아래 데이터 추출:
   - 도서 ID, 제목, 저자, 언어, 주제 등 메타데이터
   - 커버 이미지 URL
   - EPUB 파일 다운로드 링크
3. 추출된 데이터를 아래 경로에 맞게 로컬에 저장:
   - **메타데이터**: `./metadata/{book_id}.json`
   - **EPUB**: `./ebooks/{book_id}.epub`
   - **커버 이미지**: `./covers/{book_id}.jpg`

---

### Phase 2: 메타데이터 번역 (Translation)

- **담당 스크립트**: `translate.py`
- **핵심 기능**: 수집된 메타데이터(`*.json`)에 한국어 번역 필드를 추가한다.

**작업 내용:**

1. `./metadata/` 디렉토리의 모든 `.json` 파일을 순회.
2. `googletrans`와 같은 라이브러리를 사용하여 원문 필드를 번역하고, `_kor` 접미사를 붙여 새로운 필드로 추가.
   - `title` → `title_kor`
   - `author` → `author_kor`
   - `subject` → `subject_kor`
3. 번역이 완료된 JSON 객체를 원본 파일에 덮어쓴다.

---

### Phase 3: 데이터베이스 및 스토리지 업데이트 (Update)

- **담당 스크립트**: `update.py`
- **핵심 기능**: 로컬 데이터를 원격 DB 및 스토리지에 중복 없이 삽입/업로드한다.

**작업 내용:**

1. **Firebase Storage**:
   - `./ebooks/`와 `./covers/` 디렉토리의 파일을 Firebase Storage에 업로드.
   - 경로: `ebooks/{book_id}.epub`, `covers/{book_id}.jpg`
   - 업로드 후, 각 파일의 public URL을 확보.
2. **MySQL**:
   - `./metadata/`의 JSON 파일을 읽어 DB에 저장할 데이터를 준비. (Firebase에서 받은 URL 포함)
   - `book_id`를 기준으로 DB에 이미 존재하는지 확인 후, 신규 데이터만 `INSERT`.

---

### Phase 4: 설정 관리 (Configuration)

- **담당 파일**: `.env` 및 `config.py`
- **핵심 기능**: DB 접속 정보, Firebase 키 등 민감 정보를 소스 코드와 분리.
- **구현 방식**: `python-dotenv` 라이브러리를 사용하여 `.env` 파일의 환경 변수를 `config.py`에서 읽어와 파이썬 객체로 제공.

---

## 🚀 4. 고도화 및 안정성 강화 방안

- **오류 처리**: `try-except`를 활용해 특정 데이터(이미지, EPUB) 누락 시에도 전체 프로세스가 중단되지 않도록 방어 로직을 구현한다.
- **성능 개선**: `asyncio`와 `aiohttp`를 사용하여 I/O 병목을 최소화하고 크롤링 속도를 높인다.
- **로깅**: `logging` 모듈을 사용하여 파일 기반 로그를 기록, 문제 발생 시 추적 및 디버깅을 용이하게 한다.
- **상태 관리**: 이미 처리된 도서 ID를 별도 파일(`processed_ids.txt`)에 기록하여, 스크립트 중단 후 재시작 시 중복 작업을 방지한다.

---

## 📂 5. 최종 디렉토리 구조

gutenberg-crawler/

│

├── .env # 환경 변수 설정 파일

├── config.py # 설정 로딩 스크립트

├── crawl.py # Phase 1: 데이터 수집

├── translate.py # Phase 2: 번역

├── update.py # Phase 3: DB/스토리지 업데이트

├── processed_ids.txt # (고도화) 처리 완료된 ID 목록

├── crawler.log # (고도화) 로그 파일

│

├── ebooks/ # EPUB 파일 저장소

│ └── 1.epub, ...

│

├── metadata/ # JSON 메타데이터 저장소

│ └── 1.json, ...

│

└── covers/ # 커버 이미지 저장소

└── 1.jpg, ...
