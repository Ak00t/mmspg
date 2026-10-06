DELIMITER $$
DROP PROCEDURE IF EXISTS sp_get_merchant_sales_ledger_report$$
CREATE PROCEDURE sp_get_merchant_sales_ledger_report(
    IN p_merchant_id BINARY(16), IN p_start_date DATETIME(6), IN p_end_date DATETIME(6))
BEGIN
    SELECT BIN_TO_UUID(l.ledger_id) AS ledgerId, t.transaction_reference AS transactionReference,
           t.order_id AS orderId, l.entry_type AS entryType, l.balance_type AS balanceType,
           l.amount AS amount, l.description AS description, t.status AS transactionStatus,
           s.settlement_reference AS settlementReference, l.created_at AS createdAt,
           s.settlement_date AS settlementDate
    FROM merchant_ledger_entries l
    LEFT JOIN payment_transactions t ON t.transaction_id = l.transaction_id
    LEFT JOIN settlements s ON s.settlement_id = l.settlement_id
    WHERE l.merchant_id = p_merchant_id
      AND (p_start_date IS NULL OR l.created_at >= p_start_date)
      AND (p_end_date IS NULL OR l.created_at <= p_end_date)
    ORDER BY l.created_at DESC;
END$$
DELIMITER ;
