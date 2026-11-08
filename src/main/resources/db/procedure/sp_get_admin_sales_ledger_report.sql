DELIMITER $$
DROP PROCEDURE IF EXISTS sp_get_admin_sales_ledger_report$$
CREATE PROCEDURE sp_get_admin_sales_ledger_report(
    IN p_merchant_id BINARY(16),
    IN p_start_date DATETIME(6),
    IN p_end_date DATETIME(6),
    IN p_status VARCHAR(50)
)
BEGIN
    SELECT BIN_TO_UUID(l.ledger_id) AS ledgerId,
           m.merchant_code AS merchantCode,
           m.business_name AS merchantBusinessName,
           m.email AS merchantEmail,
           m.phone AS merchantPhone,
           t.transaction_reference AS transactionReference,
           t.order_id AS orderId,
           l.entry_type AS entryType,
           l.balance_type AS balanceType,
           l.amount AS amount,
           l.description AS description,
           t.status AS transactionStatus,
           s.settlement_reference AS settlementReference,
           l.created_at AS createdAt,
           s.settlement_date AS settlementDate
    FROM merchant_ledger_entries l
    INNER JOIN merchants m ON m.merchant_id = l.merchant_id
    LEFT JOIN payment_transactions t ON t.transaction_id = l.transaction_id
    LEFT JOIN settlements s ON s.settlement_id = l.settlement_id OR s.transaction_id = t.transaction_id
    WHERE (p_merchant_id IS NULL OR l.merchant_id = p_merchant_id)
      AND (p_start_date IS NULL OR l.created_at >= p_start_date)
      AND (p_end_date IS NULL OR l.created_at <= p_end_date)
      AND (p_status IS NULL OR p_status = '' OR p_status = 'ALL' OR t.status = p_status)
    ORDER BY l.created_at DESC;
END$$
DELIMITER ;
