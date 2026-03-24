CREATE TABLE tb_quote_like (
    like_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    quote_id      BIGINT NOT NULL,
    subscriber_id BIGINT NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quote_id) REFERENCES tb_quote(quote_id),
    FOREIGN KEY (subscriber_id) REFERENCES tb_subscriber(subscriber_id),
    UNIQUE KEY uq_quote_subscriber (quote_id, subscriber_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tb_saved_quote (
    saved_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscriber_id BIGINT NOT NULL,
    quote_id      BIGINT NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscriber_id) REFERENCES tb_subscriber(subscriber_id),
    FOREIGN KEY (quote_id) REFERENCES tb_quote(quote_id),
    UNIQUE KEY uq_sub_quote (subscriber_id, quote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
