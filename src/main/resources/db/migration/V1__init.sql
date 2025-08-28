
CREATE TABLE IF NOT EXISTS feature_flag (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            flag_key VARCHAR(100) NOT NULL,         -- e.g., 'checkout.newPayment'
                                            env VARCHAR(20) NOT NULL,                -- e.g., 'prod', 'stage'
                                            description VARCHAR(255) NULL,
                                            enabled TINYINT(1) NOT NULL DEFAULT 0,   -- BOOLEAN alias in MariaDB
                                            rollout_percentage TINYINT UNSIGNED NULL, -- 0~100; NULL means off/not used
                                            rules_json JSON NULL,                    -- for early experiments; will normalize later
                                            version INT NOT NULL DEFAULT 1,          -- optimistic versioning (app-level)
                                            created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                            updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                            CONSTRAINT ux_flag_key_env UNIQUE (flag_key, env)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional seed (uncomment to try quickly)
-- INSERT INTO feature_flag(flag_key, env, description, enabled, rollout_percentage)
-- VALUES ('checkout.newPayment', 'stage', '새 결제 플로우 스테이지 테스트', 0, NULL);
