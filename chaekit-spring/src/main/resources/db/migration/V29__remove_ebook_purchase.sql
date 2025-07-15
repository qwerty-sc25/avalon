-- 1. 테이블 이름 변경
RENAME TABLE ebook_purchase TO ebook_shelf_item;

-- 2. 외래키 제약 조건 제거
ALTER TABLE ebook_shelf_item
    DROP FOREIGN KEY FK_ebook_purchase_to_transaction;

-- 3. 컬럼 삭제
ALTER TABLE ebook_shelf_item
    DROP COLUMN credit_usage_transaction_id;