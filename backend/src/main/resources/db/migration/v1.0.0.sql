CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT uk_app_user_email UNIQUE (email),
    CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'FINANCE_USER', 'AUDITOR', 'OPERATIONS_USER'))
);

CREATE TABLE depot (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(32)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_depot_code UNIQUE (code)
);

CREATE TABLE terminal (
    id                     BIGSERIAL PRIMARY KEY,
    terminal_number        VARCHAR(32)  NOT NULL,
    serial_number          VARCHAR(64)  NOT NULL,
    status                 VARCHAR(32)  NOT NULL DEFAULT 'HEALTHY',
    last_sync_time         TIMESTAMPTZ,
    last_transaction_time  TIMESTAMPTZ,
    pending_transaction_count INTEGER NOT NULL DEFAULT 0,
    retry_count            INTEGER NOT NULL DEFAULT 0,
    active                 BOOLEAN NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_terminal_number UNIQUE (terminal_number),
    CONSTRAINT uk_terminal_serial UNIQUE (serial_number),
    CONSTRAINT ck_terminal_status CHECK (status IN (
        'ONLINE', 'OFFLINE', 'SYNC_DELAYED', 'SYNC_FAILED', 'SHUTDOWN_MISSED', 'HEALTHY'
    ))
);

CREATE TABLE terminal_depot_assignment (
    id           BIGSERIAL PRIMARY KEY,
    terminal_id  BIGINT NOT NULL REFERENCES terminal (id),
    depot_id     BIGINT NOT NULL REFERENCES depot (id),
    valid_from   DATE NOT NULL,
    valid_to     DATE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_assignment_range CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_assignment_terminal_from ON terminal_depot_assignment (terminal_id, valid_from);
CREATE INDEX idx_assignment_depot ON terminal_depot_assignment (depot_id);
CREATE UNIQUE INDEX uk_assignment_open_per_terminal
    ON terminal_depot_assignment (terminal_id)
    WHERE valid_to IS NULL;

CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    action          VARCHAR(64)  NOT NULL,
    entity_type     VARCHAR(64)  NOT NULL,
    entity_id       VARCHAR(64),
    actor_username  VARCHAR(64),
    details_json    JSONB,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_created ON audit_log (created_at DESC);

CREATE TABLE card_transaction (
    id                      BIGSERIAL PRIMARY KEY,
    transaction_reference   VARCHAR(64)    NOT NULL,
    approval_number         VARCHAR(64)    NOT NULL,
    card_alias              VARCHAR(64)    NOT NULL,
    terminal_id             BIGINT         NOT NULL REFERENCES terminal (id),
    transaction_type        VARCHAR(32)    NOT NULL,
    product_type            VARCHAR(64),
    amount                  NUMERIC(19, 2) NOT NULL,
    transaction_time        TIMESTAMPTZ    NOT NULL,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    source_system           VARCHAR(64)    NOT NULL DEFAULT 'CSV_IMPORT',
    CONSTRAINT uk_card_transaction_reference UNIQUE (transaction_reference),
    CONSTRAINT ck_card_transaction_type CHECK (transaction_type IN (
        'SALE', 'CANCELLATION', 'LOAD', 'WITHDRAWAL', 'REFUND', 'TRANSFER', 'CORRECTION'
    )),
    CONSTRAINT ck_card_transaction_amount CHECK (amount >= 0)
);

CREATE INDEX idx_card_tx_terminal_time
    ON card_transaction (terminal_id, transaction_time);

CREATE UNIQUE INDEX uk_card_tx_duplicate_fingerprint
    ON card_transaction (approval_number, card_alias, terminal_id, amount);

CREATE INDEX idx_card_tx_type_time
    ON card_transaction (transaction_type, transaction_time);

CREATE TABLE financial_period (
    id               BIGSERIAL PRIMARY KEY,
    depot_id         BIGINT         NOT NULL REFERENCES depot (id),
    period_date      DATE           NOT NULL,
    opening_balance  NUMERIC(19, 2) NOT NULL,
    deposited_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    withdrawal_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    actual_closing_balance NUMERIC(19, 2),
    status           VARCHAR(32)    NOT NULL DEFAULT 'OPEN',
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    version          BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_financial_period_depot_date UNIQUE (depot_id, period_date),
    CONSTRAINT ck_financial_period_status CHECK (status IN ('OPEN', 'RECONCILED', 'CLOSED'))
);

CREATE TABLE reconciliation_result (
    id                   BIGSERIAL PRIMARY KEY,
    financial_period_id  BIGINT         NOT NULL REFERENCES financial_period (id),
    expected_closing_balance NUMERIC(19, 2) NOT NULL,
    actual_closing_balance   NUMERIC(19, 2) NOT NULL,
    variance             NUMERIC(19, 2) NOT NULL,
    sale_amount          NUMERIC(19, 2) NOT NULL DEFAULT 0,
    cancellation_amount  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    net_amount           NUMERIC(19, 2) NOT NULL DEFAULT 0,
    status               VARCHAR(32)    NOT NULL DEFAULT 'MATCHED',
    resolution_note      VARCHAR(1024),
    resolved_by          VARCHAR(64),
    resolved_at          TIMESTAMPTZ,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    version              BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT ck_reconciliation_status CHECK (status IN ('MATCHED', 'SMALL_VARIANCE', 'LARGE_VARIANCE', 'RESOLVED'))
);

CREATE INDEX idx_reconciliation_period ON reconciliation_result (financial_period_id);
CREATE INDEX idx_reconciliation_status ON reconciliation_result (status);

CREATE TABLE anomaly (
    id               BIGSERIAL PRIMARY KEY,
    rule_code        VARCHAR(32)  NOT NULL,
    severity         VARCHAR(16)  NOT NULL,
    status           VARCHAR(32)  NOT NULL DEFAULT 'OPEN',
    entity_type      VARCHAR(64)  NOT NULL,
    entity_id        VARCHAR(64)  NOT NULL,
    title            VARCHAR(256) NOT NULL,
    details          TEXT,
    detected_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    reviewed_by      VARCHAR(64),
    resolution_note  VARCHAR(1024),
    resolved_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT ck_anomaly_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT ck_anomaly_status CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED'))
);

CREATE INDEX idx_anomaly_status_severity ON anomaly (status, severity);
CREATE INDEX idx_anomaly_entity ON anomaly (entity_type, entity_id);
CREATE INDEX idx_anomaly_rule ON anomaly (rule_code, detected_at DESC);

CREATE TABLE report_snapshot (
    id              BIGSERIAL PRIMARY KEY,
    report_type     VARCHAR(64)  NOT NULL,
    parameters_json TEXT         NOT NULL,
    status          VARCHAR(32)  NOT NULL DEFAULT 'RUNNING',
    result_hash     VARCHAR(128),
    output_path     VARCHAR(512),
    error_message   TEXT,
    requested_by    VARCHAR(64),
    started_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT ck_report_snapshot_status CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_report_snapshot_status ON report_snapshot (status, started_at DESC);
CREATE INDEX idx_report_snapshot_type_hash ON report_snapshot (report_type, result_hash);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = current_user) THEN
        EXECUTE format('REVOKE DELETE ON TABLE audit_log FROM %I', current_user);
    END IF;
EXCEPTION
    WHEN insufficient_privilege THEN
        RAISE NOTICE 'Skipping REVOKE DELETE on audit_log (insufficient privilege)';
    WHEN undefined_object THEN
        RAISE NOTICE 'Skipping REVOKE DELETE on audit_log (role/object missing)';
END $$;

INSERT INTO app_user (username, email, password_hash, role, enabled) VALUES
('admin',   'admin@example.com',   '$2a$10$CshVZbJtK7lSY7DsmtX/NeOSWyC.IBNLksObGHY0oiiPyVOdR1vcy', 'ADMIN', TRUE),
('finance', 'finance@example.com', '$2a$10$CshVZbJtK7lSY7DsmtX/NeOSWyC.IBNLksObGHY0oiiPyVOdR1vcy', 'FINANCE_USER', TRUE),
('auditor', 'auditor@example.com', '$2a$10$CshVZbJtK7lSY7DsmtX/NeOSWyC.IBNLksObGHY0oiiPyVOdR1vcy', 'AUDITOR', TRUE),
('ops',     'ops@example.com',     '$2a$10$CshVZbJtK7lSY7DsmtX/NeOSWyC.IBNLksObGHY0oiiPyVOdR1vcy', 'OPERATIONS_USER', TRUE);

INSERT INTO depot (code, name, active) VALUES
('NEL', 'Nelspruit Demo Depot', TRUE),
('WHR', 'White River Demo Depot', TRUE),
('JHB', 'Johannesburg Demo Depot', TRUE);

INSERT INTO terminal (terminal_number, serial_number, status, last_sync_time, last_transaction_time, active) VALUES
('3122', 'SN-3122-DEMO', 'HEALTHY', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '3 hours', TRUE),
('4001', 'SN-4001-DEMO', 'SYNC_DELAYED', NOW() - INTERVAL '30 hours', NOW() - INTERVAL '31 hours', TRUE),
('5100', 'SN-5100-DEMO', 'OFFLINE', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', TRUE);

INSERT INTO terminal_depot_assignment (terminal_id, depot_id, valid_from, valid_to)
SELECT t.id, d.id, DATE '2026-03-01', DATE '2026-03-31'
FROM terminal t, depot d
WHERE t.terminal_number = '3122' AND d.code = 'NEL';

INSERT INTO terminal_depot_assignment (terminal_id, depot_id, valid_from, valid_to)
SELECT t.id, d.id, DATE '2026-04-01', NULL
FROM terminal t, depot d
WHERE t.terminal_number = '3122' AND d.code = 'WHR';

INSERT INTO terminal_depot_assignment (terminal_id, depot_id, valid_from, valid_to)
SELECT t.id, d.id, DATE '2026-01-01', NULL
FROM terminal t, depot d
WHERE t.terminal_number = '4001' AND d.code = 'JHB';

INSERT INTO terminal_depot_assignment (terminal_id, depot_id, valid_from, valid_to)
SELECT t.id, d.id, DATE '2026-01-01', NULL
FROM terminal t, depot d
WHERE t.terminal_number = '5100' AND d.code = 'NEL';
