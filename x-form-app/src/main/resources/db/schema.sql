CREATE TABLE IF NOT EXISTS x_form_definition (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '表单 ID',
    share_code VARCHAR(32) NOT NULL COMMENT '公开分享码',
    title VARCHAR(120) NOT NULL COMMENT '表单名称',
    description VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '表单说明',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' COMMENT '表单状态',
    fields_json LONGTEXT NOT NULL COMMENT '表单字段定义 JSON',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_x_form_share_code (share_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单定义';

CREATE TABLE IF NOT EXISTS x_form_submission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交记录 ID',
    form_id BIGINT NOT NULL COMMENT '表单 ID',
    answers_json LONGTEXT NOT NULL COMMENT '用户填写内容 JSON',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (id),
    KEY idx_x_form_submission_form_time (form_id, submitted_at),
    CONSTRAINT fk_x_form_submission_definition
        FOREIGN KEY (form_id) REFERENCES x_form_definition (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单提交记录';
