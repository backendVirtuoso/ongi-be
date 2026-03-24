-- 어드민 대시보드 쿼리 성능 향상을 위한 인덱스
ALTER TABLE tb_subscriber ADD INDEX idx_subscribed_at (subscribed_at);
ALTER TABLE tb_send_history ADD INDEX idx_sent_at (sent_at);
