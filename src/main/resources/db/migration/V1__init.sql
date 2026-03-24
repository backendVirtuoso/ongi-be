-- ─── 문장 테이블 ───
CREATE TABLE tb_quote (
    quote_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    content       TEXT NOT NULL COMMENT '문장 내용',
    category      VARCHAR(20) NOT NULL COMMENT '카테고리: COMFORT, CHEER, ENCOURAGE, SUPPORT',
    source_type   VARCHAR(10) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL | AI',
    like_count    INT DEFAULT 0 COMMENT '좋아요 수',
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_active (category, is_active),
    INDEX idx_source_type (source_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─── 구독자 테이블 ───
CREATE TABLE tb_subscriber (
    subscriber_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일 주소',
    name            VARCHAR(50) NULL COMMENT '이름 (선택)',
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION' COMMENT 'ACTIVE | PAUSED | UNSUBSCRIBED | PENDING_VERIFICATION',
    preferred_cats  JSON NULL COMMENT '선호 카테고리 ["COMFORT","CHEER"]',
    verify_token    VARCHAR(100) NULL COMMENT '이메일 인증 토큰',
    verified_at     DATETIME NULL COMMENT '인증 완료 시간',
    subscribed_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    unsubscribed_at DATETIME NULL,
    INDEX idx_status (status),
    INDEX idx_email (email),
    INDEX idx_verify_token (verify_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─── 발송 이력 테이블 ───
CREATE TABLE tb_send_history (
    history_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscriber_id   BIGINT NOT NULL,
    quote_id        BIGINT NOT NULL,
    send_type       VARCHAR(10) NOT NULL COMMENT 'MORNING | EVENING',
    send_status     VARCHAR(10) NOT NULL DEFAULT 'SUCCESS' COMMENT 'SUCCESS | FAILED | RETRY',
    sent_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    error_message   TEXT NULL,
    FOREIGN KEY (subscriber_id) REFERENCES tb_subscriber(subscriber_id),
    FOREIGN KEY (quote_id) REFERENCES tb_quote(quote_id),
    INDEX idx_subscriber_date (subscriber_id, sent_at),
    INDEX idx_send_type_date (send_type, sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
