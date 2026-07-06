-- Patch for db_platform order APIs.
-- Run this once if service_order.debt_status or service_order_item.staff_id is missing.

ALTER TABLE service_order
    ADD COLUMN debt_status TINYINT NOT NULL DEFAULT 0 COMMENT 'Debt status: 0 none, 1 unpaid/installment, 2 settled'
        AFTER debt_amount;

ALTER TABLE service_order_item
    ADD COLUMN staff_id BIGINT NULL COMMENT 'Service staff id'
        AFTER actual_amount;
