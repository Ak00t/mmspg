-- Sample Data Script for MMSPG
-- Adds sample merchants, branches, terminals, transactions, settlements, and ledger entries.

USE mmspg;

-- Ensure Staff ID exists for FK
SET @staff_id = (SELECT staff_id FROM staff_users LIMIT 1);
SET @mcc_id = (SELECT mcc_id FROM mcc_codes LIMIT 1);
SET @pwd_hash = '$2a$10$NE6Jwl4nwIfX1noyCm2J/OzBilfaqmTar/mfZe2sxgElkTarV1qEe';

-- 1. Insert Merchant 1: Ocean Supercenter (MC-000001)
SET @m1_id = UUID_TO_BIN('01a1bc01-0000-0000-0000-000000000001');
INSERT INTO merchants (merchant_id, merchant_code, business_name, contact_name, email, phone, settlement_account_no, status, password_hash, mcc_id, created_at, updated_at)
VALUES (@m1_id, 'MC-000001', 'Ocean Supercenter', 'U Kyaw Swar', 'ocean@gmail.com', '09987654321', '2001122334', 'ACTIVE', @pwd_hash, @mcc_id, '2026-10-01 09:00:00', '2026-10-01 09:00:00')
ON DUPLICATE KEY UPDATE business_name = VALUES(business_name);

-- 2. Insert Merchant 2: Shwe Taung Pharmacy (MC-000002)
SET @m2_id = UUID_TO_BIN('01a1bc02-0000-0000-0000-000000000002');
INSERT INTO merchants (merchant_id, merchant_code, business_name, contact_name, email, phone, settlement_account_no, status, password_hash, mcc_id, created_at, updated_at)
VALUES (@m2_id, 'MC-000002', 'Shwe Taung Pharmacy', 'Daw Khin Khin', 'shwetaung@gmail.com', '09776655443', '2005566778', 'ACTIVE', @pwd_hash, @mcc_id, '2026-10-10 10:00:00', '2026-10-10 10:00:00')
ON DUPLICATE KEY UPDATE business_name = VALUES(business_name);

-- 3. Insert Merchant 3: KFC Myanmar (MC-000003)
SET @m3_id = UUID_TO_BIN('01a1bc03-0000-0000-0000-000000000003');
INSERT INTO merchants (merchant_id, merchant_code, business_name, contact_name, email, phone, settlement_account_no, status, password_hash, mcc_id, created_at, updated_at)
VALUES (@m3_id, 'MC-000003', 'KFC Myanmar', 'Ko Min Min', 'kfc@gmail.com', '09445566778', '2009988776', 'ACTIVE', @pwd_hash, @mcc_id, '2026-10-15 11:00:00', '2026-10-15 11:00:00')
ON DUPLICATE KEY UPDATE business_name = VALUES(business_name);

-- 4. Branches
SET @b1_id = UUID_TO_BIN('01a1bc01-b001-0000-0000-000000000001');
INSERT INTO merchant_branches (branch_id, merchant_id, branch_code, branch_name, phone, address, city, status, created_by, created_at, updated_at)
VALUES (@b1_id, @m1_id, 'BR-MC-000001-01', 'Ocean Hledan Branch', '09987654321', 'Pyay Road, Kamayut', 'Yangon', 'ACTIVE', @staff_id, '2026-10-01 09:30:00', '2026-10-01 09:30:00')
ON DUPLICATE KEY UPDATE branch_name = VALUES(branch_name);

SET @b2_id = UUID_TO_BIN('01a1bc02-b001-0000-0000-000000000001');
INSERT INTO merchant_branches (branch_id, merchant_id, branch_code, branch_name, phone, address, city, status, created_by, created_at, updated_at)
VALUES (@b2_id, @m2_id, 'BR-MC-000002-01', 'Shwe Taung Main Branch', '09776655443', 'Bogyoke Road, Pabedan', 'Yangon', 'ACTIVE', @staff_id, '2026-10-10 10:30:00', '2026-10-10 10:30:00')
ON DUPLICATE KEY UPDATE branch_name = VALUES(branch_name);

SET @b3_id = UUID_TO_BIN('01a1bc03-b001-0000-0000-000000000001');
INSERT INTO merchant_branches (branch_id, merchant_id, branch_code, branch_name, phone, address, city, status, created_by, created_at, updated_at)
VALUES (@b3_id, @m3_id, 'BR-MC-000003-01', 'KFC Junction City Branch', '09445566778', 'Junction City Level 4, Pabedan', 'Yangon', 'ACTIVE', @staff_id, '2026-10-15 11:30:00', '2026-10-15 11:30:00')
ON DUPLICATE KEY UPDATE branch_name = VALUES(branch_name);

-- 5. Terminals
SET @t1_id = UUID_TO_BIN('01a1bc01-e001-0000-0000-000000000001');
INSERT INTO terminals (terminal_id, merchant_id, branch_id, terminal_code, terminal_name, terminal_type, status, created_by, created_at, updated_at)
VALUES (@t1_id, @m1_id, @b1_id, 'POS-000001', 'Ocean Cashier 1', 'PHYSICAL_POS', 'ONLINE', @staff_id, '2026-10-01 10:00:00', '2026-10-01 10:00:00')
ON DUPLICATE KEY UPDATE terminal_name = VALUES(terminal_name);

SET @t2_id = UUID_TO_BIN('01a1bc02-e001-0000-0000-000000000001');
INSERT INTO terminals (terminal_id, merchant_id, branch_id, terminal_code, terminal_name, terminal_type, status, created_by, created_at, updated_at)
VALUES (@t2_id, @m2_id, @b2_id, 'POS-000002', 'Shwe Taung Counter 1', 'PHYSICAL_POS', 'ONLINE', @staff_id, '2026-10-10 11:00:00', '2026-10-10 11:00:00')
ON DUPLICATE KEY UPDATE terminal_name = VALUES(terminal_name);

SET @t3_id = UUID_TO_BIN('01a1bc03-e001-0000-0000-000000000001');
INSERT INTO terminals (terminal_id, merchant_id, branch_id, terminal_code, terminal_name, terminal_type, status, created_by, created_at, updated_at)
VALUES (@t3_id, @m3_id, @b3_id, 'API-000001', 'KFC Online API Terminal', 'VIRTUAL_API', 'ONLINE', @staff_id, '2026-10-15 12:00:00', '2026-10-15 12:00:00')
ON DUPLICATE KEY UPDATE terminal_name = VALUES(terminal_name);

-- Helper procedure to add a complete transaction, settlement, and ledger entry
DELIMITER $$
DROP PROCEDURE IF EXISTS sp_add_sample_transaction$$
CREATE PROCEDURE sp_add_sample_transaction(
    IN p_merchant_id BINARY(16),
    IN p_branch_id BINARY(16),
    IN p_terminal_id BINARY(16),
    IN p_txn_ref VARCHAR(100),
    IN p_order_id VARCHAR(100),
    IN p_amount DECIMAL(18,4),
    IN p_fee DECIMAL(18,4),
    IN p_status VARCHAR(50),
    IN p_date_str VARCHAR(30)
)
BEGIN
    DECLARE v_txn_id BINARY(16);
    DECLARE v_stl_id BINARY(16);
    DECLARE v_ldg_id BINARY(16);
    DECLARE v_net DECIMAL(18,4);
    DECLARE v_dt DATETIME(6);
    DECLARE v_settle_date DATE;
    DECLARE v_bank_acc VARCHAR(100);

    SET v_txn_id = UUID_TO_BIN(UUID());
    SET v_stl_id = UUID_TO_BIN(UUID());
    SET v_ldg_id = UUID_TO_BIN(UUID());
    SET v_net = p_amount - p_fee;
    SET v_dt = STR_TO_DATE(p_date_str, '%Y-%m-%d %H:%i:%s');
    SET v_settle_date = DATE(v_dt);

    SELECT settlement_account_no INTO v_bank_acc FROM merchants WHERE merchant_id = p_merchant_id;
    IF v_bank_acc IS NULL THEN
        SET v_bank_acc = '2001122334';
    END IF;

    -- 1. Insert Payment Transaction
    INSERT INTO payment_transactions (
        transaction_id, merchant_id, branch_id, terminal_id,
        transaction_reference, order_id, amount, fee_amount, net_amount,
        currency, status, initiated_at, authorized_at, completed_at,
        created_at, updated_at, payment_token
    ) VALUES (
        v_txn_id, p_merchant_id, p_branch_id, p_terminal_id,
        p_txn_ref, p_order_id, p_amount, p_fee, v_net,
        'MMK', p_status, v_dt, v_dt,
        IF(p_status = 'COMPLETED', v_dt, NULL),
        v_dt, v_dt, CONCAT('TOKEN-', UUID())
    );

    -- 2. If COMPLETED, insert Settlement & Ledger Entry
    IF p_status = 'COMPLETED' THEN
        INSERT INTO settlements (
            settlement_id, merchant_id, transaction_id,
            settlement_reference, settlement_date, gross_amount, fee_amount, net_amount,
            currency, bank_account_no, status, refund_amount, processed_at, created_at, updated_at
        ) VALUES (
            v_stl_id, p_merchant_id, v_txn_id,
            CONCAT('STL-', UPPER(SUBSTRING(HEX(v_stl_id), 1, 16))),
            v_settle_date, p_amount, p_fee, v_net,
            'MMK', v_bank_acc, 'COMPLETED', 0.0000, v_dt, v_dt, v_dt
        );

        INSERT INTO merchant_ledger_entries (
            ledger_id, merchant_id, transaction_id, settlement_id,
            entry_type, balance_type, amount, description, created_at
        ) VALUES (
            v_ldg_id, p_merchant_id, v_txn_id, v_stl_id,
            'CREDIT', 'SETTLED', v_net,
            CONCAT('Payment settlement for Order ID: ', p_order_id),
            v_dt
        );
    END IF;
END$$
DELIMITER ;

-- Insert sample transactions for Ocean Supercenter (MC-000001)
CALL sp_add_sample_transaction(@m1_id, @b1_id, @t1_id, 'TXN-OCN-20261101-001', 'ORD-OCN-101', 45000.0000, 450.0000, 'COMPLETED', '2026-11-01 10:15:00');
CALL sp_add_sample_transaction(@m1_id, @b1_id, @t1_id, 'TXN-OCN-20261102-002', 'ORD-OCN-102', 125000.0000, 1250.0000, 'COMPLETED', '2026-11-02 11:30:00');
CALL sp_add_sample_transaction(@m1_id, @b1_id, @t1_id, 'TXN-OCN-20261103-003', 'ORD-OCN-103', 78000.0000, 780.0000, 'COMPLETED', '2026-11-03 14:20:00');
CALL sp_add_sample_transaction(@m1_id, @b1_id, @t1_id, 'TXN-OCN-20261104-004', 'ORD-OCN-104', 32000.0000, 320.0000, 'COMPLETED', '2026-11-04 16:45:00');
CALL sp_add_sample_transaction(@m1_id, @b1_id, @t1_id, 'TXN-OCN-20261105-005', 'ORD-OCN-105', 150000.0000, 1500.0000, 'COMPLETED', '2026-11-05 18:00:00');
CALL sp_add_sample_transaction(@m1_id, @b1_id, @t1_id, 'TXN-OCN-20261106-006', 'ORD-OCN-106', 22000.0000, 220.0000, 'FAILED', '2026-11-06 19:10:00');
CALL sp_add_sample_transaction(@m1_id, @b1_id, @t1_id, 'TXN-OCN-20261107-007', 'ORD-OCN-107', 95000.0000, 950.0000, 'COMPLETED', '2026-11-07 12:15:00');

-- Insert sample transactions for Shwe Taung Pharmacy (MC-000002)
CALL sp_add_sample_transaction(@m2_id, @b2_id, @t2_id, 'TXN-STP-20261101-001', 'ORD-STP-201', 18500.0000, 185.0000, 'COMPLETED', '2026-11-01 09:40:00');
CALL sp_add_sample_transaction(@m2_id, @b2_id, @t2_id, 'TXN-STP-20261102-002', 'ORD-STP-202', 36000.0000, 360.0000, 'COMPLETED', '2026-11-02 13:10:00');
CALL sp_add_sample_transaction(@m2_id, @b2_id, @t2_id, 'TXN-STP-20261104-003', 'ORD-STP-203', 54000.0000, 540.0000, 'COMPLETED', '2026-11-04 15:30:00');
CALL sp_add_sample_transaction(@m2_id, @b2_id, @t2_id, 'TXN-STP-20261106-004', 'ORD-STP-204', 12000.0000, 120.0000, 'COMPLETED', '2026-11-06 17:00:00');
CALL sp_add_sample_transaction(@m2_id, @b2_id, @t2_id, 'TXN-STP-20261107-005', 'ORD-STP-205', 82000.0000, 820.0000, 'COMPLETED', '2026-11-07 19:25:00');

-- Insert sample transactions for KFC Myanmar (MC-000003)
CALL sp_add_sample_transaction(@m3_id, @b3_id, @t3_id, 'TXN-KFC-20261101-001', 'ORD-KFC-301', 28500.0000, 285.0000, 'COMPLETED', '2026-11-01 12:00:00');
CALL sp_add_sample_transaction(@m3_id, @b3_id, @t3_id, 'TXN-KFC-20261102-002', 'ORD-KFC-302', 42000.0000, 420.0000, 'COMPLETED', '2026-11-02 18:30:00');
CALL sp_add_sample_transaction(@m3_id, @b3_id, @t3_id, 'TXN-KFC-20261103-003', 'ORD-KFC-303', 19500.0000, 195.0000, 'COMPLETED', '2026-11-03 13:15:00');
CALL sp_add_sample_transaction(@m3_id, @b3_id, @t3_id, 'TXN-KFC-20261105-004', 'ORD-KFC-304', 65000.0000, 650.0000, 'COMPLETED', '2026-11-05 19:45:00');
CALL sp_add_sample_transaction(@m3_id, @b3_id, @t3_id, 'TXN-KFC-20261106-005', 'ORD-KFC-305', 31000.0000, 310.0000, 'COMPLETED', '2026-11-06 20:10:00');
CALL sp_add_sample_transaction(@m3_id, @b3_id, @t3_id, 'TXN-KFC-20261107-006', 'ORD-KFC-306', 58000.0000, 580.0000, 'COMPLETED', '2026-11-07 14:00:00');

-- Clean up helper procedure
DROP PROCEDURE IF EXISTS sp_add_sample_transaction;
