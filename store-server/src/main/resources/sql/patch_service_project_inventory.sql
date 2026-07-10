-- Patch for service project inventory consumption mapping.
-- Run this once before enabling automatic stock deduction on completed service orders.

CREATE TABLE IF NOT EXISTS service_project_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    service_project_id BIGINT NOT NULL COMMENT 'Service project id',
    inventory_id BIGINT NOT NULL COMMENT 'Inventory SKU id',
    consume_quantity DECIMAL(10, 2) NOT NULL COMMENT 'Default quantity consumed per service',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 0 disabled, 1 enabled',
    remark VARCHAR(500) NULL COMMENT 'Remark',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_service_project_inventory (service_project_id, inventory_id),
    KEY idx_spi_project (service_project_id),
    KEY idx_spi_inventory (inventory_id),
    CONSTRAINT fk_spi_service_project
        FOREIGN KEY (service_project_id) REFERENCES service_project (id),
    CONSTRAINT fk_spi_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventory_sku (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Service project inventory consumption mapping';
