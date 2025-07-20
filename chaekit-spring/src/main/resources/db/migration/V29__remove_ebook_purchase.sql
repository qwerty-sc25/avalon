-- 1. 테이블 이름 변경
RENAME TABLE ebook_purchase TO ebook_shelf_item;

-- 2. 외래키 제약 조건 제거
ALTER TABLE ebook_shelf_item
    DROP FOREIGN KEY FK_ebook_purchase_to_transaction;

-- 3. 컬럼 삭제
ALTER TABLE ebook_shelf_item
    DROP COLUMN credit_usage_transaction_id;

ALTER TABLE ebook
    DROP COLUMN price,
    DROP COLUMN publisher_id,
    DROP FOREIGN KEY FK4c0e4rpnkldvcpvwx4hn2ouu9;

DROP TABLE ebook_request;
DROP TABLE credit_payment_transaction;
DROP TABLE credit_usage_transaction;
DROP TABLE credit_wallet;

ALTER TABLE notification
    DROP COLUMN publisher_id,
    DROP FOREIGN KEY FK_notification_publisher;

DROP TABLE publisher_profile;
