-- Spring Data JDBC DDL

-- Account table
CREATE TABLE IF NOT EXISTS account (
    id VARCHAR(8) PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    mail VARCHAR(256) NOT NULL,
    status_type VARCHAR(20) NOT NULL
);

-- Financial institution account table
CREATE TABLE IF NOT EXISTS fi_account (
    id VARCHAR(32) PRIMARY KEY,
    account_id VARCHAR(8) NOT NULL,
    category VARCHAR(30) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    fi_code VARCHAR(32) NOT NULL,
    fi_account_id VARCHAR(8) NOT NULL
);

-- Self financial institution account table
CREATE TABLE IF NOT EXISTS self_fi_account (
    id VARCHAR(32) PRIMARY KEY,
    category VARCHAR(30) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    fi_code VARCHAR(32) NOT NULL,
    fi_account_id VARCHAR(8) NOT NULL
);

-- Cash balance table
CREATE TABLE IF NOT EXISTS cash_balance (
    id VARCHAR(32) PRIMARY KEY,
    account_id VARCHAR(8) NOT NULL,
    base_day DATE NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount DECIMAL(20,4) NOT NULL,
    update_date TIMESTAMP NOT NULL
);

-- Cashflow table
CREATE TABLE IF NOT EXISTS cashflow (
    id VARCHAR(32) PRIMARY KEY,
    account_id VARCHAR(8) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount DECIMAL(20,4) NOT NULL,
    cashflow_type VARCHAR(20) NOT NULL,
    remark VARCHAR(30),
    event_day DATE NOT NULL,
    event_date TIMESTAMP NOT NULL,
    value_day DATE NOT NULL,
    status_type VARCHAR(20) NOT NULL,
    update_actor VARCHAR(8) NOT NULL,
    update_date TIMESTAMP NOT NULL
);

-- Cash in/out table
CREATE TABLE IF NOT EXISTS cash_in_out (
    id VARCHAR(32) PRIMARY KEY,
    account_id VARCHAR(8) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    abs_amount DECIMAL(20,4) NOT NULL,
    withdrawal BOOLEAN NOT NULL,
    request_day DATE NOT NULL,
    request_date TIMESTAMP NOT NULL,
    event_day DATE NOT NULL,
    value_day DATE NOT NULL,
    target_fi_code VARCHAR(32) NOT NULL,
    target_fi_account_id VARCHAR(8) NOT NULL,
    self_fi_code VARCHAR(32) NOT NULL,
    self_fi_account_id VARCHAR(8) NOT NULL,
    status_type VARCHAR(20) NOT NULL,
    update_actor VARCHAR(8) NOT NULL,
    update_date TIMESTAMP NOT NULL,
    cashflow_id VARCHAR(32)
);
 