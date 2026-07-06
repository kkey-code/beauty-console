# store-pojo 重构检查清单

本次 `store-pojo` 按新的数据库设计重新编写，没有从旧项目移动 DTO、VO、Entity 或枚举代码。

## 包结构

```text
com.wkr.storepojo
├─ dto
├─ entity
├─ enums
└─ vo
```

## Entity 与表名

| Entity | 表名 | 说明 |
| --- | --- | --- |
| `SysUser` | `sys_user` | 用户账号表 |
| `StaffMember` | `staff_member` | 员工表 |
| `CustomerProfile` | `customer_profile` | 客户档案表 |
| `ServiceProject` | `service_project` | 服务项目表 |
| `Appointment` | `appointment` | 预约主表 |
| `AppointmentItem` | `appointment_item` | 预约项目明细表 |
| `ServiceOrder` | `service_order` | 订单主表 |
| `ServiceOrderItem` | `service_order_item` | 订单项目明细表 |
| `PaymentRecord` | `payment_record` | 收款流水表 |
| `InventorySku` | `inventory_sku` | 库存物品表 |
| `InventoryStockLog` | `inventory_stock_log` | 库存流水表 |

## DTO 检查

- `PageQueryDTO` 作为分页查询基础参数。
- 用户：`SysUserDTO`、`SysUserLoginDTO`、`SysUserPageQueryDTO`。
- 员工：`StaffMemberDTO`、`StaffMemberPageQueryDTO`。
- 客户：`CustomerProfileDTO`、`CustomerProfilePageQueryDTO`。
- 服务项目：`ServiceProjectDTO`、`ServiceProjectPageQueryDTO`。
- 预约：`AppointmentDTO`、`AppointmentItemDTO`、`AppointmentPageQueryDTO`。
- 订单：`ServiceOrderDTO`、`ServiceOrderItemDTO`、`ServiceOrderPageQueryDTO`、`OrderPaymentDTO`。
- 收款：`PaymentRecordDTO`、`PaymentRecordPageQueryDTO`。
- 库存：`InventorySkuDTO`、`InventorySkuPageQueryDTO`、`InventoryStockLogDTO`、`InventoryStockLogPageQueryDTO`。

## VO 检查

- 登录：`LoginUserVO`。
- 用户：`SysUserVO`。
- 员工：`StaffMemberVO`。
- 客户：`CustomerProfileVO`。
- 服务项目：`ServiceProjectVO`。
- 预约：`AppointmentVO`、`AppointmentItemVO`。
- 订单：`ServiceOrderVO`、`ServiceOrderItemVO`。
- 收款：`PaymentRecordVO`。
- 库存：`InventorySkuVO`、`InventoryStockLogVO`。

## 枚举检查

| Enum | 用途 |
| --- | --- |
| `CommonStatusEnum` | 通用启停状态 |
| `GenderEnum` | 性别 |
| `RoleCodeEnum` | 用户角色 |
| `CustomerLevelEnum` | 客户等级 |
| `AppointmentStatusEnum` | 预约状态 |
| `OrderTypeEnum` | 订单类型 |
| `OrderStatusEnum` | 订单状态 |
| `PayStatusEnum` | 订单支付状态 |
| `DebtStatusEnum` | 欠款状态 |
| `PaymentMethodEnum` | 支付方式 |
| `PaymentRecordStatusEnum` | 收款流水状态 |
| `InventoryChangeTypeEnum` | 库存变动类型 |

## 后续 server 层接入检查

- `store-server/src/main/resources/application.yml` 中 `mybatis-plus.type-aliases-package` 应为 `com.wkr.storepojo.entity`。
- Mapper 泛型需要引用 `com.wkr.storepojo.entity.*`。
- Controller 入参需要引用 `com.wkr.storepojo.dto.*`。
- Controller 出参需要引用 `com.wkr.storepojo.vo.*`。
- 订单和预约不要再把多个服务项目放在主表字段里，应通过 `appointment_item` 和 `service_order_item` 维护明细。
- 收款不要直接只改订单金额，应同时写入 `payment_record`。
- 库存变动不要只改当前库存，应同时写入 `inventory_stock_log`。

## 编译检查命令

```bash
mvn -pl store-pojo -am test -DskipTests
```
