-- Drop tables in dependency order
DROP TABLE IF EXISTS statuses CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS budgets CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS userDetails CASCADE;

-- User Details Table (Profile info)
CREATE TABLE IF NOT EXISTS userDetails (
      id VARCHAR(50) PRIMARY KEY,     -- You provide this (could be UUID or external auth ID)
      first_name VARCHAR(50) NOT NULL,
      last_name VARCHAR(50),
      date_of_birth DATE
);

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20),
    icon VARCHAR(50),
    color VARCHAR(20),
    CONSTRAINT fk_category_user
        FOREIGN KEY (user_id)
        REFERENCES userDetails(id)
        ON DELETE CASCADE
);

-- Main accounts table (common fields)
 CREATE TABLE IF NOT EXISTS accounts (
     id BIGSERIAL PRIMARY KEY,
     user_id VARCHAR(50) NOT NULL,
     type VARCHAR(20) NOT NULL CHECK (type IN ('BANK', 'CREDIT_CARD', 'CASH', 'LOAN', 'INVESTMENT', 'FRIEND')),
     display_name VARCHAR(50),
     balance NUMERIC(12,2) DEFAULT 0,
     currency VARCHAR(10),
     status VARCHAR(20) DEFAULT 'active',
     created_at TIMESTAMP DEFAULT NOW(),
     CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES userDetails(id) ON DELETE CASCADE
 );


-- Credit card details
CREATE TABLE IF NOT EXISTS credit_card_details(
    account_id BIGINT PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
    outstanding NUMERIC(12,2) DEFAULT 0,
    credit_limit NUMERIC(12,2),
    due_date DATE,
    bill_date DATE
);

 -- EMI loan details (linked by account_id)
 CREATE TABLE IF NOT EXISTS emi_account_details (
     account_id BIGINT PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
     principal_amount NUMERIC(12,2) NOT NULL,
     interest_rate NUMERIC(5,2) NOT NULL, -- annual %
     total_installments INT NOT NULL,
     paid_installments INT DEFAULT 0,
     emi_amount NUMERIC(12,2) NOT NULL,
     start_date DATE NOT NULL,
     end_date DATE,
     next_due_date DATE
 );

 -- Investment details (linked by account_id)
 CREATE TABLE IF NOT EXISTS investment_account_details (
     account_id BIGINT PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
     investment_type VARCHAR(50) NOT NULL,
     purchase_date DATE NOT NULL,
     maturity_date DATE,
     amount_invested NUMERIC(12,2) NOT NULL,
     expected_return NUMERIC(12,2),
     current_value NUMERIC(12,2)
 );

 -- Friend/Loan details (linked by account_id)
 CREATE TABLE IF NOT EXISTS friend_account_details (
     account_id BIGINT PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
     friend_name VARCHAR(100),
     contact_info VARCHAR(255),
     outstanding_amount NUMERIC(12,2) DEFAULT 0,
     loan_start_date DATE,
     loan_due_date DATE
 );


CREATE TABLE IF NOT EXISTS budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    category_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL, -- 1-12
    amount NUMERIC(12,2) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    recurrence VARCHAR(20) DEFAULT 'monthly',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uq_budget UNIQUE (user_id, category_id, year, month),
    CONSTRAINT fk_budget_user
        FOREIGN KEY (user_id)
        REFERENCES userDetails(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE CASCADE
);


-- Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    account_id BIGINT NOT NULL,
    debit_credit VARCHAR(10) CHECK (debit_credit IN ('debit', 'credit')),
    amount NUMERIC(12,2) NOT NULL,
    category_id BIGINT,
    description VARCHAR(255),
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    account_balance NUMERIC(12,2),
    status VARCHAR(20) DEFAULT 'completed',
    status_id BIGINT,
    CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id)
        REFERENCES userDetails(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE SET NULL
);

-- Statuses Table
CREATE TABLE IF NOT EXISTS statuses (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('pending_in', 'pending_out', 'completed', 'cancelled', 'disputed')),
    description VARCHAR(255),
    due_date DATE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_status_user
        FOREIGN KEY (user_id)
        REFERENCES userDetails(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_status_from_account
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_status_to_account
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_accounts_not_same CHECK (from_account_id IS DISTINCT FROM to_account_id)
);

-- Useful Indexes
CREATE INDEX IF NOT EXISTS idx_transactions_user_created ON transactions(user_id, created);
CREATE INDEX IF NOT EXISTS idx_budgets_user_category_period ON budgets(user_id, category_id, year, month);
CREATE INDEX IF NOT EXISTS idx_statuses_user_status ON statuses(user_id, status);
