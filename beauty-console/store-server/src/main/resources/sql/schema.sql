-- =============================================
-- 数据库名称：db_platform
-- 字符集：utf8mb4
-- =============================================

CREATE DATABASE IF NOT EXISTS `db_platform` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `db_platform`;

-- =============================================
-- 1. sys_user 用户账号表
-- =============================================
CREATE TABLE `sys_user` (
    `id` bigint NOT NULL COMMENT '主键',
    `username` varchar(50) NOT NULL COMMENT '登录账号',
    `password_hash` varchar(100) NOT NULL COMMENT '登录密码/密码哈希',
    `role_code` varchar(20) NOT NULL DEFAULT 'readonly' COMMENT '角色：admin/staff/readonly',
    `staff_id` bigint DEFAULT NULL COMMENT '关联员工ID',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_sys_user_staff` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

-- =============================================
-- 2. staff_member 员工表
-- =============================================
CREATE TABLE `staff_member` (
    `id` bigint NOT NULL COMMENT '主键',
    `name` varchar(50) NOT NULL COMMENT '员工姓名',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `gender` tinyint DEFAULT NULL COMMENT '性别：1男，2女',
    `position` varchar(50) DEFAULT NULL COMMENT '职位',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用，1启用',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

-- =============================================
-- 3. customer_profile 客户档案表
-- =============================================
CREATE TABLE `customer_profile` (
    `id` bigint NOT NULL COMMENT '主键',
    `name` varchar(50) NOT NULL COMMENT '客户姓名',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `gender` tinyint DEFAULT NULL COMMENT '性别：1男，2女',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `level` varchar(20) NOT NULL DEFAULT '普通' COMMENT '客户等级：普通/银卡/金卡/VIP',
    `source` varchar(50) DEFAULT NULL COMMENT '客户来源',
    `owner_staff_id` bigint DEFAULT NULL COMMENT '建档员工ID，用于员工数据范围',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_level` (`level`),
    KEY `idx_customer_owner_staff` (`owner_staff_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户档案表';

-- =============================================
-- 4. service_project 服务项目表
-- =============================================
CREATE TABLE `service_project` (
    `id` bigint NOT NULL COMMENT '主键',
    `name` varchar(100) NOT NULL COMMENT '项目名称',
    `category` varchar(50) DEFAULT NULL COMMENT '项目分类',
    `price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '标准价格',
    `duration_minutes` int NOT NULL DEFAULT 0 COMMENT '服务时长，单位分钟',
    `description` varchar(500) DEFAULT NULL COMMENT '项目说明',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0下架，1上架',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

-- =============================================
-- 5. appointment 预约主表
-- =============================================
CREATE TABLE `appointment` (
    `id` bigint NOT NULL COMMENT '主键',
    `appointment_no` varchar(64) NOT NULL COMMENT '预约编号',
    `customer_id` bigint NOT NULL COMMENT '客户ID',
    `staff_id` bigint NOT NULL COMMENT '主服务员工ID',
    `appointment_time` datetime NOT NULL COMMENT '预约时间',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待确认，1已确认，2已完成，3已取消',
    `total_duration_minutes` int NOT NULL DEFAULT 0 COMMENT '预计总时长',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_appointment_no` (`appointment_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_appointment_time` (`appointment_time`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约主表';

-- =============================================
-- 6. appointment_item 预约项目明细表
-- =============================================
CREATE TABLE `appointment_item` (
    `id` bigint NOT NULL COMMENT '主键',
    `appointment_id` bigint NOT NULL COMMENT '预约ID',
    `service_project_id` bigint NOT NULL COMMENT '服务项目ID',
    `staff_id` bigint DEFAULT NULL COMMENT '指定服务员工ID',
    `service_name` varchar(100) NOT NULL COMMENT '项目快照名称',
    `price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '项目快照价格',
    `duration_minutes` int NOT NULL DEFAULT 0 COMMENT '项目快照时长',
    `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_appointment_id` (`appointment_id`),
    KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约项目明细表';

-- =============================================
-- 7. service_order 订单主表
-- =============================================
CREATE TABLE `service_order` (
    `id` bigint NOT NULL COMMENT '主键',
    `order_no` varchar(64) NOT NULL COMMENT '订单编号',
    `appointment_id` bigint DEFAULT NULL COMMENT '关联预约ID，可为空',
    `customer_id` bigint NOT NULL COMMENT '客户ID',
    `order_type` varchar(20) NOT NULL COMMENT '订单类型：服务/次卡/护理卡/会员卡/疗程卡',
    `original_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '原价金额',
    `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    `receivable_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '应收金额',
    `paid_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '已收金额',
    `debt_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '欠款金额',
    `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态：0未支付，1部分支付，2已支付，3已退款',
    `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态：0待服务，1已完成，2已取消',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_appointment_id` (`appointment_id`),
    KEY `idx_pay_status` (`pay_status`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- =============================================
-- 8. service_order_item 订单项目明细表
-- =============================================
CREATE TABLE `service_order_item` (
    `id` bigint NOT NULL COMMENT '主键',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `service_project_id` bigint DEFAULT NULL COMMENT '服务项目ID',
    `service_name` varchar(100) NOT NULL COMMENT '项目快照名称',
    `unit_price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    `quantity` decimal(10,2) NOT NULL DEFAULT 1.00 COMMENT '数量',
    `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '明细优惠金额',
    `actual_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '明细实收/应收金额',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项目明细表';

-- =============================================
-- 9. payment_record 收款流水表
-- =============================================
CREATE TABLE `payment_record` (
    `id` bigint NOT NULL COMMENT '主键',
    `order_id` bigint NOT NULL COMMENT '订单ID',
    `payment_no` varchar(64) NOT NULL COMMENT '收款流水号',
    `payment_method` varchar(20) NOT NULL COMMENT '支付方式：微信/支付宝/现金/次卡',
    `pay_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '收款金额',
    `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0未确认，1成功，2退款，3作废',
    `pay_time` datetime DEFAULT NULL COMMENT '收款时间',
    `operator_id` bigint NOT NULL COMMENT '操作人ID',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_pay_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收款流水表';

-- =============================================
-- 10. inventory_sku 库存物品表
-- =============================================
CREATE TABLE `inventory_sku` (
    `id` bigint NOT NULL COMMENT '主键',
    `name` varchar(64) NOT NULL COMMENT '库存名称',
    `category` varchar(64) DEFAULT NULL COMMENT '分类',
    `unit` varchar(20) DEFAULT NULL COMMENT '单位',
    `quantity` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '当前库存',
    `safety_stock` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '安全库存',
    `cost_price` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '成本价',
    `supplier` varchar(100) DEFAULT NULL COMMENT '供应商',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0停用，1启用',
    `remark` varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存物品表';

-- =============================================
-- 11. inventory_stock_log 库存流水表
-- =============================================
CREATE TABLE `inventory_stock_log` (
    `id` bigint NOT NULL COMMENT '主键',
    `inventory_id` bigint NOT NULL COMMENT '库存物品ID',
    `change_type` varchar(20) NOT NULL COMMENT '类型：入库/出库/盘点/报损/退货',
    `change_quantity` decimal(12,2) NOT NULL COMMENT '变动数量',
    `before_quantity` decimal(12,2) NOT NULL COMMENT '变动前库存',
    `after_quantity` decimal(12,2) NOT NULL COMMENT '变动后库存',
    `related_order_id` bigint DEFAULT NULL COMMENT '关联订单ID，可为空',
    `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_inventory_id` (`inventory_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';
