-- Spring Data JDBC initial data
-- Basic master data and sample data

-- Clear existing data
DELETE FROM cash_balance;
DELETE FROM fi_account;
DELETE FROM self_fi_account;
DELETE FROM account;

-- Sample accounts
INSERT INTO account (id, name, mail, status_type) VALUES 
('sample', 'sample', 'hoge@example.com', 'NORMAL');

-- Self financial institution accounts (master data)
INSERT INTO self_fi_account (id, category, currency, fi_code, fi_account_id) VALUES 
('self_cash_out_jpy', 'cashOut', 'JPY', 'cashOut-JPY', 'xxxxxx');

-- Sample financial institution accounts
INSERT INTO fi_account (id, account_id, category, currency, fi_code, fi_account_id) VALUES 
('fi_sample_cash_out_jpy', 'sample', 'cashOut', 'JPY', 'cashOut-JPY', 'FIsample');

-- Sample balance data
INSERT INTO cash_balance (id, account_id, base_day, currency, amount, update_date) VALUES 
('bal_sample_jpy', 'sample', CURRENT_DATE, 'JPY', 1000000.0000, CURRENT_TIMESTAMP); 