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

INSERT INTO service_project_inventory (
    id, service_project_id, inventory_id, consume_quantity, status, remark, create_time, update_time
) VALUES
    (2001, 1005, 2012, 0.50, 1, '小气泡清洁液', NOW(), NOW()),
    (2002, 1005, 2001, 0.20, 1, '洁面棉片', NOW(), NOW()),
    (2003, 1005, 2002, 1.00, 1, '一次性面膜碗', NOW(), NOW()),
    (2004, 1005, 2006, 0.10, 1, '一次性手套', NOW(), NOW()),
    (2005, 1005, 2007, 0.20, 1, '消毒湿巾', NOW(), NOW()),
    (2006, 1010, 2001, 0.30, 1, '基础清洁棉片', NOW(), NOW()),
    (2007, 1010, 2010, 0.20, 1, '卸妆棉', NOW(), NOW()),
    (2008, 1010, 2002, 1.00, 1, '一次性面膜碗', NOW(), NOW()),
    (2009, 1010, 2014, 1.00, 1, '护理小毛巾', NOW(), NOW()),
    (2010, 1011, 2003, 0.20, 1, '玻尿酸导入精华', NOW(), NOW()),
    (2011, 1011, 2004, 1.00, 1, '舒缓修护面膜', NOW(), NOW()),
    (2012, 1011, 2008, 0.10, 1, '海藻颗粒面膜', NOW(), NOW()),
    (2013, 1011, 2002, 1.00, 1, '一次性面膜碗', NOW(), NOW()),
    (2014, 1012, 2004, 1.00, 1, '敏感肌修护面膜', NOW(), NOW()),
    (2015, 1012, 2009, 0.30, 1, '舒缓冷敷凝胶', NOW(), NOW()),
    (2016, 1012, 2007, 0.20, 1, '消毒湿巾', NOW(), NOW()),
    (2017, 1018, 2003, 0.10, 1, '眼部导入精华', NOW(), NOW()),
    (2018, 1018, 2014, 1.00, 1, '护理小毛巾', NOW(), NOW()),
    (2019, 1019, 2015, 0.20, 1, '芳疗基础油', NOW(), NOW()),
    (2020, 1019, 2005, 0.20, 1, '美容床一次性床单', NOW(), NOW()),
    (2021, 1019, 2014, 2.00, 1, '护理小毛巾', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    consume_quantity = VALUES(consume_quantity),
    status = VALUES(status),
    remark = VALUES(remark),
    update_time = NOW();
