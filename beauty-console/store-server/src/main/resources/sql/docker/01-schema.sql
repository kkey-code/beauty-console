SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS db_platform
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_platform;

CREATE TABLE IF NOT EXISTS staff_member (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    gender      TINYINT      DEFAULT NULL,
    position    VARCHAR(50)  DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    remark      VARCHAR(500) DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_staff_phone (phone),
    KEY idx_staff_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工';

CREATE TABLE IF NOT EXISTS sys_role (
    id          INT          NOT NULL,
    role_code   VARCHAR(50)  NOT NULL,
    role_name   VARCHAR(50)  NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色';

INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
    (1, 'SUPER_ADMIN', '超级管理员', '全部管理权限', 1),
    (2, 'STORE_MANAGER', '店长', '门店运营和员工管理', 1),
    (3, 'STAFF', '普通员工', '客户、预约和服务订单', 1),
    (4, 'INVENTORY_ADMIN', '库存管理员', '库存和出入库管理', 1),
    (5, 'FINANCE', '财务/收银', '收款和财务操作', 1),
    (6, 'READONLY', '只读', '只读访问', 1)
ON DUPLICATE KEY UPDATE
    role_code = VALUES(role_code),
    role_name = VALUES(role_name),
    description = VALUES(description),
    status = VALUES(status);

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT       NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    role_id         INT          NOT NULL,
    staff_id        BIGINT       DEFAULT NULL,
    status          TINYINT      NOT NULL DEFAULT 1,
    last_login_time DATETIME     DEFAULT NULL,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_role (role_id),
    UNIQUE KEY uk_sys_user_staff (staff_id),
    KEY idx_sys_user_status (status),
    CONSTRAINT fk_sys_user_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS customer_profile (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    phone       VARCHAR(20)  DEFAULT NULL,
    gender      TINYINT      DEFAULT NULL,
    birthday    DATE         DEFAULT NULL,
    level       TINYINT      NOT NULL DEFAULT 0,
    source      VARCHAR(50)  DEFAULT NULL,
    owner_staff_id BIGINT    DEFAULT NULL,
    remark      VARCHAR(500) DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_customer_phone (phone),
    KEY idx_customer_level (level),
    KEY idx_customer_owner_staff (owner_staff_id),
    KEY idx_customer_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户档案';

CREATE TABLE IF NOT EXISTS service_project (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    name             VARCHAR(100)  NOT NULL,
    category         VARCHAR(50)   DEFAULT NULL,
    price            DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    duration_minutes INT           NOT NULL DEFAULT 0,
    description      VARCHAR(500)  DEFAULT NULL,
    status           TINYINT       NOT NULL DEFAULT 1,
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_service_project_category (category),
    KEY idx_service_project_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目';

CREATE TABLE IF NOT EXISTS inventory_sku (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    name         VARCHAR(100)  NOT NULL,
    category     VARCHAR(64)   DEFAULT NULL,
    unit         VARCHAR(20)   DEFAULT NULL,
    quantity     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    safety_stock DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    cost_price   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    supplier     VARCHAR(100)  DEFAULT NULL,
    status       TINYINT       NOT NULL DEFAULT 1,
    remark       VARCHAR(255)  DEFAULT NULL,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_inventory_category (category),
    KEY idx_inventory_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存物品';

CREATE TABLE IF NOT EXISTS appointment (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    appointment_no         VARCHAR(64)  NOT NULL,
    customer_id            BIGINT       NOT NULL,
    staff_id               BIGINT       DEFAULT NULL,
    appointment_time       DATETIME     NOT NULL,
    status                 TINYINT      NOT NULL DEFAULT 0,
    total_duration_minutes INT          NOT NULL DEFAULT 0,
    remark                 VARCHAR(500) DEFAULT NULL,
    create_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_appointment_no (appointment_no),
    KEY idx_appointment_customer (customer_id),
    KEY idx_appointment_staff (staff_id),
    KEY idx_appointment_time (appointment_time),
    KEY idx_appointment_status (status),
    KEY idx_appointment_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约主表';

CREATE TABLE IF NOT EXISTS appointment_item (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    appointment_id   BIGINT        NOT NULL,
    service_project_id BIGINT      NOT NULL,
    staff_id         BIGINT        DEFAULT NULL,
    service_name     VARCHAR(100)  DEFAULT NULL,
    price            DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    duration_minutes INT           NOT NULL DEFAULT 0,
    sort_no          INT           NOT NULL DEFAULT 0,
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_appointment_item_appointment (appointment_id),
    KEY idx_appointment_item_project (service_project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约项目明细';

CREATE TABLE IF NOT EXISTS service_order (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    order_no          VARCHAR(64)   NOT NULL,
    request_id        VARCHAR(36)   DEFAULT NULL,
    appointment_id    BIGINT        DEFAULT NULL,
    customer_id       BIGINT        NOT NULL,
    order_type        VARCHAR(20)   NOT NULL,
    original_amount   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    receivable_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    paid_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    debt_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    debt_status       TINYINT       NOT NULL DEFAULT 0,
    pay_status        TINYINT       NOT NULL DEFAULT 0,
    order_status      TINYINT       NOT NULL DEFAULT 0,
    remark            VARCHAR(500)  DEFAULT NULL,
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_service_order_no (order_no),
    UNIQUE KEY uk_service_order_request_id (request_id),
    UNIQUE KEY uk_service_order_appointment (appointment_id),
    KEY idx_service_order_customer (customer_id),
    KEY idx_service_order_status (order_status),
    KEY idx_service_order_pay_status (pay_status),
    KEY idx_service_order_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务订单';

CREATE TABLE IF NOT EXISTS service_order_item (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    order_id           BIGINT        NOT NULL,
    service_project_id BIGINT        NOT NULL,
    service_name       VARCHAR(100)  DEFAULT NULL,
    unit_price         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    quantity           DECIMAL(12,2) NOT NULL DEFAULT 1.00,
    discount_amount    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    actual_amount      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    staff_id           BIGINT        DEFAULT NULL,
    remark             VARCHAR(500)  DEFAULT NULL,
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_service_order_item_order (order_id),
    KEY idx_service_order_item_project (service_project_id),
    KEY idx_service_order_item_staff (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项目明细';

CREATE TABLE IF NOT EXISTS payment_record (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    order_id       BIGINT        NOT NULL,
    payment_no     VARCHAR(64)   NOT NULL,
    payment_method VARCHAR(20)   NOT NULL,
    pay_amount     DECIMAL(12,2) NOT NULL,
    pay_status     TINYINT       NOT NULL DEFAULT 0,
    pay_time       DATETIME      DEFAULT NULL,
    operator_id    BIGINT        DEFAULT NULL,
    remark         VARCHAR(500)  DEFAULT NULL,
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_payment_order (order_id),
    KEY idx_payment_status (pay_status),
    KEY idx_payment_time (pay_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收款流水';

CREATE TABLE IF NOT EXISTS inventory_stock_log (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    inventory_id     BIGINT        NOT NULL,
    change_type      VARCHAR(30)   NOT NULL,
    change_quantity  DECIMAL(12,2) NOT NULL,
    before_quantity  DECIMAL(12,2) NOT NULL,
    after_quantity   DECIMAL(12,2) NOT NULL,
    related_order_id BIGINT        DEFAULT NULL,
    operator_id      BIGINT        DEFAULT NULL,
    remark           VARCHAR(500)  DEFAULT NULL,
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_stock_log_inventory (inventory_id),
    KEY idx_stock_log_type (change_type),
    KEY idx_stock_log_order (related_order_id),
    KEY idx_stock_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水';

CREATE TABLE IF NOT EXISTS service_project_inventory (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    service_project_id BIGINT        NOT NULL,
    inventory_id       BIGINT        NOT NULL,
    consume_quantity   DECIMAL(10,2) NOT NULL,
    status             TINYINT       NOT NULL DEFAULT 1,
    remark             VARCHAR(500)  DEFAULT NULL,
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_service_project_inventory (service_project_id, inventory_id),
    KEY idx_project_inventory_project (service_project_id),
    KEY idx_project_inventory_inventory (inventory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目耗材关系';

CREATE TABLE IF NOT EXISTS operation_audit_log (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    operator_user_id  BIGINT       DEFAULT NULL,
    operator_staff_id BIGINT       DEFAULT NULL,
    action_type       VARCHAR(64)  NOT NULL,
    target_type       VARCHAR(64)  NOT NULL,
    target_id         BIGINT       DEFAULT NULL,
    detail            VARCHAR(500) DEFAULT NULL,
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_audit_operator (operator_user_id),
    KEY idx_audit_target (target_type, target_id),
    KEY idx_audit_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志';
