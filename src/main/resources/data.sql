INSERT INTO public.userdetails(id, first_name, last_name, date_of_birth)
VALUES ('1', 'Test', 'Test', '2025-10-24');

DELETE FROM public.credit_card_details;
DELETE FROM public.emi_account_details;
DELETE FROM public.investment_account_details;
DELETE FROM public.friend_account_details;

INSERT INTO public.accounts(user_id, type, display_name, balance, currency, status)
VALUES
    ('1', 'BANK', 'Seed Checking', 12400.00, 'INR', 'active'),
    ('1', 'CASH', 'Seed Cash', 800.00, 'INR', 'active'),
    ('1', 'CREDIT_CARD', 'Seed Credit Card', 4200.00, 'INR', 'active'),
    ('1', 'LOAN', 'Seed Loan', 12000.00, 'INR', 'active'),
    ('1', 'INVESTMENT', 'Seed Investment', 15500.00, 'INR', 'active'),
    ('1', 'FRIEND', 'Seed Friend', 2500.00, 'INR', 'active');

INSERT INTO public.credit_card_details(account_id, outstanding, credit_limit, due_date, bill_date)
SELECT id, 4200.00, 20000.00, CURRENT_DATE + 10, CURRENT_DATE - 5
FROM public.accounts
WHERE user_id = '1' AND display_name = 'Seed Credit Card';

INSERT INTO public.emi_account_details(
    account_id, principal_amount, interest_rate, total_installments, paid_installments,
    emi_amount, start_date, end_date, next_due_date
)
SELECT id, 25000.00, 9.50, 24, 8, 1200.00,
       CURRENT_DATE - INTERVAL '8 months',
       CURRENT_DATE + INTERVAL '16 months',
       CURRENT_DATE + 4
FROM public.accounts
WHERE user_id = '1' AND display_name = 'Seed Loan';

INSERT INTO public.investment_account_details(
    account_id, investment_type, purchase_date, maturity_date,
    amount_invested, expected_return, current_value
)
SELECT id, 'Mutual Fund', CURRENT_DATE - INTERVAL '12 months',
       CURRENT_DATE + INTERVAL '12 months', 13000.00, 8.00, 15500.00
FROM public.accounts
WHERE user_id = '1' AND display_name = 'Seed Investment';

INSERT INTO public.friend_account_details(
    account_id, friend_name, contact_info, outstanding_amount, loan_start_date, loan_due_date
)
SELECT id, 'Test Friend', 'test.friend@example.com', 2500.00,
       CURRENT_DATE - INTERVAL '2 months', CURRENT_DATE + INTERVAL '1 month'
FROM public.accounts
WHERE user_id = '1' AND display_name = 'Seed Friend';

WITH sample_accounts AS (
    SELECT account.id AS account_id, account.user_id, sample.start_balance, sample.current_balance
    FROM public.accounts account
    JOIN (
        VALUES
            ('Seed Checking', 8000.00::NUMERIC, 12400.00::NUMERIC),
            ('Seed Cash', 300.00::NUMERIC, 800.00::NUMERIC),
            ('Seed Credit Card', 1800.00::NUMERIC, 4200.00::NUMERIC),
            ('Seed Loan', 18000.00::NUMERIC, 12000.00::NUMERIC),
            ('Seed Investment', 11000.00::NUMERIC, 15500.00::NUMERIC),
            ('Seed Friend', 4000.00::NUMERIC, 2500.00::NUMERIC)
    ) AS sample(display_name, start_balance, current_balance)
        ON sample.display_name = account.display_name
    WHERE account.user_id = '1'
),
timeline AS (
    SELECT sample_accounts.*,
           snapshot.snapshot_order,
           snapshot.description,
           snapshot.created
    FROM sample_accounts
    CROSS JOIN LATERAL (
        VALUES
            (1, 'Year baseline', date_trunc('year', CURRENT_DATE::TIMESTAMP) - INTERVAL '1 day' + INTERVAL '00:01:00'),
            (2, 'Month baseline', date_trunc('month', CURRENT_DATE::TIMESTAMP) - INTERVAL '1 day' + INTERVAL '06:00:00'),
            (3, 'Week baseline', date_trunc('week', CURRENT_DATE::TIMESTAMP) - INTERVAL '1 day' + INTERVAL '12:00:00'),
            (4, 'Yesterday snapshot', CURRENT_DATE::TIMESTAMP - INTERVAL '1 day' + INTERVAL '18:00:00'),
            (5, 'Current balance snapshot', CURRENT_DATE::TIMESTAMP + INTERVAL '00:01:00')
    ) AS snapshot(snapshot_order, description, created)
),
positioned AS (
    SELECT timeline.*,
           ROW_NUMBER() OVER (
               PARTITION BY account_id
               ORDER BY created, snapshot_order
           ) AS position,
           COUNT(*) OVER (PARTITION BY account_id) AS snapshot_count
    FROM timeline
),
balances AS (
    SELECT positioned.*,
           ROUND(
               start_balance +
               (current_balance - start_balance) *
               (position - 1)::NUMERIC / NULLIF(snapshot_count - 1, 0),
               2
           ) AS account_balance
    FROM positioned
),
changes AS (
    SELECT balances.*,
           LAG(account_balance) OVER (
               PARTITION BY account_id
               ORDER BY created, snapshot_order
           ) AS previous_balance
    FROM balances
)
INSERT INTO public.transactions(
    user_id, account_id, debit_credit, amount, description, created, account_balance, status
)
SELECT user_id,
       account_id,
       CASE
           WHEN previous_balance IS NULL OR account_balance >= previous_balance THEN 'credit'
           ELSE 'debit'
       END,
       CASE
           WHEN previous_balance IS NULL THEN account_balance
           ELSE ABS(account_balance - previous_balance)
       END,
       'Test history: ' || description,
       created,
       account_balance,
       'completed'
FROM changes;

