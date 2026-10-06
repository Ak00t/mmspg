DELIMITER $$
DROP PROCEDURE IF EXISTS sp_get_merchant_settlement_report$$
CREATE PROCEDURE sp_get_merchant_settlement_report(
    IN p_merchant_id BINARY(16), IN p_start_date DATE, IN p_end_date DATE)
BEGIN
    SELECT BIN_TO_UUID(s.settlement_id) AS settlementId, s.settlement_reference AS settlementReference,
           t.transaction_reference AS transactionReference, s.settlement_date AS settlementDate,
           s.gross_amount AS grossAmount, s.fee_amount AS feeAmount, s.net_amount AS netAmount,
           s.bank_account_no AS bankAccountNo, s.status AS status, s.processed_at AS processedAt,
           s.created_at AS createdAt
    FROM settlements s
    LEFT JOIN payment_transactions t ON t.transaction_id = s.transaction_id
    WHERE s.merchant_id = p_merchant_id
      AND (p_start_date IS NULL OR s.settlement_date >= p_start_date)
      AND (p_end_date IS NULL OR s.settlement_date <= p_end_date)
    ORDER BY s.settlement_date DESC, s.created_at DESC;
END$$
DELIMITER ;
