-- 구독자 테이블에 role 컬럼 추가 (USER | ADMIN)
ALTER TABLE tb_subscriber
    ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'USER' COMMENT 'USER | ADMIN';
