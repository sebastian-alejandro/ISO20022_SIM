-- ISO 20022 Simulator Database Initialization
-- Create database schema for transaction processing

-- Create sequence for transaction IDs
CREATE SEQUENCE IF NOT EXISTS transaction_seq START 1;

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY DEFAULT nextval('transaction_seq'),
    transaction_id VARCHAR(255) UNIQUE NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    sender VARCHAR(255) NOT NULL,
    receiver VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2),
    currency VARCHAR(3),
    status VARCHAR(20) DEFAULT 'PENDING',
    raw_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY DEFAULT nextval('transaction_seq'),
    account_number VARCHAR(50) UNIQUE NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    bank_code VARCHAR(20),
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create audit log table
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY DEFAULT nextval('transaction_seq'),
    transaction_id VARCHAR(255),
    action VARCHAR(50),
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO accounts (account_number, account_name, bank_code, balance, currency) VALUES
('ACC001', 'Test Account 1', 'BANK001', 10000.00, 'USD'),
('ACC002', 'Test Account 2', 'BANK002', 5000.00, 'EUR'),
('ACC003', 'Test Account 3', 'BANK001', 15000.00, 'USD')
ON CONFLICT (account_number) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts(status);
CREATE INDEX IF NOT EXISTS idx_audit_log_transaction_id ON audit_log(transaction_id);

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;
