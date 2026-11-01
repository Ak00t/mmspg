DELIMITER $$

DROP PROCEDURE IF EXISTS sp_get_merchant_dashboard_summary$$

CREATE PROCEDURE sp_get_merchant_dashboard_summary(
    IN p_merchant_id BINARY(16),
    IN p_start_date DATETIME(6),
    IN p_end_date DATETIME(6)
)
BEGIN
    SELECT
        COALESCE(SUM(CASE
            WHEN t.status = 'COMPLETED' THEN t.amount
            ELSE 0
        END), 0) AS grossSales,
        COUNT(t.transaction_id) AS totalTransactions,
        COALESCE(SUM(CASE
            WHEN t.status = 'COMPLETED' THEN t.net_amount
            ELSE 0
        END), 0) AS availableSettlementBalance,
        COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS pendingSettlementsCount,
        COALESCE(SUM(CASE WHEN t.status = 'INITIATED' THEN 1 ELSE 0 END), 0) AS initiatedCount,
        COALESCE(SUM(CASE WHEN t.status = 'PENDING_AUTHORIZATION' THEN 1 ELSE 0 END), 0) AS pendingAuthorizationCount,
        COALESCE(SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completedCount,
        COALESCE(SUM(CASE WHEN t.status = 'FAILED' THEN 1 ELSE 0 END), 0) AS failedCount
    FROM payment_transactions t
    WHERE t.merchant_id = p_merchant_id
      AND (p_start_date IS NULL OR t.created_at >= p_start_date)
      AND (p_end_date IS NULL OR t.created_at <= p_end_date);
END$$

DELIMITER ;
