# Project Gutenberg Top 100 Crawler

This project is a Python-based web crawler that automatically collects, translates, and stores the top 100 e-books from Project Gutenberg.

## ✨ Features

- **Crawling**: Collects metadata, cover images, and EPUB files for the top 100 books.
- **Translation**: Translates metadata such as title, author, and subjects into Korean using the Google Cloud Translation API.
- **Storage**: Uploads collected files (covers, EPUBs) to Firebase Storage.
- **Database**: Stores all processed metadata and storage URLs in a MySQL database.
- **Resumable**: The process can be stopped and resumed, as it keeps track of already processed books.

## ⚙️ Prerequisites

- Python 3.8+
- A Google Cloud Platform (GCP) project with:
    - Google Cloud Translation API enabled.
    - A service account with a downloadable JSON key.
- A Firebase project with Firebase Storage enabled.
- A running MySQL server.

## 🚀 Setup and Execution

Follow these steps to set up and run the crawler.

### 1. Install Dependencies

Navigate to the `gutenberg-crawler` directory and install the required Python libraries.

```bash
pip install -r requirements.txt
```

### 2. Configure Environment

Open the `.env` file and provide the necessary information:

```dotenv
# --- MySQL Database Configuration ---
DB_HOST=127.0.0.1
DB_USER=your_db_user
DB_PASSWORD=your_db_password
DB_NAME=gutenberg
DB_PORT=3306

# --- Firebase & Google Cloud Configuration ---
# Absolute path to your Google Cloud service account JSON file
GOOGLE_APPLICATION_CREDENTIALS="C:/path/to/your/serviceAccountKey.json"

# Your Firebase Storage bucket URL (e.g., my-project-12345.appspot.com)
FIREBASE_STORAGE_BUCKET=your-project-id.appspot.com

# Your Google Cloud Project ID (for the Translation API)
GOOGLE_PROJECT_ID=your-gcp-project-id
```

### 3. Set Up Database

Connect to your MySQL server and create the database and table required for this project.

```sql
CREATE DATABASE IF NOT EXISTS gutenberg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gutenberg;

CREATE TABLE books (
    id INT PRIMARY KEY,
    title VARCHAR(512),
    author VARCHAR(512),
    language VARCHAR(50),
    subjects TEXT,
    title_kor VARCHAR(512),
    author_kor VARCHAR(512),
    subjects_kor TEXT,
    cover_url VARCHAR(1024),
    epub_url VARCHAR(1024),
    gutenberg_url VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. Run the Scripts

Execute the scripts sequentially. Each script completes one phase of the process.

```bash
# 1. Crawl data from Project Gutenberg
python crawl.py

# 2. Translate the collected metadata
python translate.py

# 3. Upload files to Storage and update the Database
python update.py
```

## 📂 Project Structure

```
gutenberg-crawler/
│
├── .env              # Environment variables (DB, Firebase credentials)
├── requirements.txt  # Python dependencies
│
├── crawl.py          # Phase 1: Collects data from the web
├── translate.py      # Phase 2: Translates metadata using Google Translate API
├── update.py         # Phase 3: Uploads files and updates the database
│
├── config.py         # Loads configuration from .env
├── processed_ids.txt # Tracks completed book IDs to prevent re-processing
├── crawler.log       # Log file for debugging
│
├── ebooks/           # Stores downloaded .epub files
├── metadata/         # Stores .json metadata files
└── covers/           # Stores downloaded cover images
```
