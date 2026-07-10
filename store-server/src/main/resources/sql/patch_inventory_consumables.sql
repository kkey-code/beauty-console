-- Add realistic beauty salon consumable inventory data.

USE db_platform;

SET NAMES utf8mb4;

INSERT INTO inventory_sku (
    id, name, category, unit, quantity, safety_stock, cost_price, supplier, status, remark, create_time, update_time
) VALUES
    (2001, '洁面棉片', 'consumable', '包', 120.00, 30.00, 18.50, '广州净颜耗材', 1, '基础清洁耗材', NOW(), NOW()),
    (2002, '一次性面膜碗', 'consumable', '个', 260.00, 80.00, 0.80, '广州净颜耗材', 1, '护理项目通用', NOW(), NOW()),
    (2003, '玻尿酸导入精华', 'material', '瓶', 35.00, 10.00, 96.00, '上海肌研供应', 1, '补水导入项目', NOW(), NOW()),
    (2004, '舒缓修护面膜', 'material', '盒', 48.00, 12.00, 58.00, '上海肌研供应', 1, '敏感肌修护', NOW(), NOW()),
    (2005, '美容床一次性床单', 'consumable', '卷', 18.00, 8.00, 32.00, '深圳安心卫材', 1, '房间通用耗材', NOW(), NOW()),
    (2006, '一次性丁腈手套', 'consumable', '盒', 52.00, 15.00, 29.00, '深圳安心卫材', 1, '操作防护', NOW(), NOW()),
    (2007, '消毒湿巾', 'consumable', '包', 75.00, 20.00, 12.00, '深圳安心卫材', 1, '工具台面消毒', NOW(), NOW()),
    (2008, '海藻颗粒面膜', 'material', '袋', 40.00, 10.00, 45.00, '杭州植萃原料', 1, '基础补水面膜', NOW(), NOW()),
    (2009, '舒缓冷敷凝胶', 'material', '支', 28.00, 8.00, 36.00, '杭州植萃原料', 1, '术后舒缓', NOW(), NOW()),
    (2010, '卸妆棉', 'consumable', '包', 90.00, 25.00, 9.50, '广州净颜耗材', 1, '清洁卸妆', NOW(), NOW()),
    (2011, '纹绣修复膏', 'material', '支', 22.00, 6.00, 42.00, '广州美纹供应', 1, '纹绣项目', NOW(), NOW()),
    (2012, '小气泡清洁液', 'material', '瓶', 26.00, 6.00, 68.00, '上海肌研供应', 1, '小气泡设备耗材', NOW(), NOW()),
    (2013, '酒精棉片', 'consumable', '盒', 66.00, 18.00, 11.00, '深圳安心卫材', 1, '消毒清洁', NOW(), NOW()),
    (2014, '护理小毛巾', 'consumable', '条', 180.00, 60.00, 3.20, '佛山布草供应', 1, '护理房通用', NOW(), NOW()),
    (2015, '芳疗基础油', 'material', '瓶', 20.00, 5.00, 88.00, '杭州植萃原料', 1, '身体护理项目', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    category = VALUES(category),
    unit = VALUES(unit),
    quantity = VALUES(quantity),
    safety_stock = VALUES(safety_stock),
    cost_price = VALUES(cost_price),
    supplier = VALUES(supplier),
    status = VALUES(status),
    remark = VALUES(remark),
    update_time = NOW();
