# 门店后台管理系统

一个面向美容门店经营场景的前后端分离后台系统，覆盖账号权限、员工、客户、服务项目、预约、订单、收款和库存耗材等业务。

- 前端：Vue 2 + TypeScript + Element UI，默认运行在 `http://localhost:8888`
- 后端：Java 17 + Spring Boot 3.5.16 + MyBatis-Plus，默认运行在 `http://localhost:8080`
- 数据：MySQL + Redis
- 接口：统一使用 `/admin` 前缀，详细字段见 [接口文档](./接口文档.md)

## 功能概览

| 模块 | 主要能力 |
| --- | --- |
| 账号与权限 | JWT 登录、六类角色、用户级权限覆盖、账号启停用 |
| 员工与客户 | 员工档案、客户资料、等级和来源维护 |
| 服务项目 | 项目维护、上下架、项目与库存耗材关系配置 |
| 预约 | 预约及明细维护、确认、完成、取消、预约转订单 |
| 服务订单 | 订单及明细维护、金额校验、完成、取消、删除保护 |
| 收款 | 收款流水、订单金额回写、并发防超收、收款作废 |
| 库存 | SKU、入库、出库、盘点、库存流水、Excel 导出 |
| 安全与审计 | BCrypt 密码、JWT 鉴权、接口权限、关键操作审计 |

核心业务链路：

```text
预约 -> 确认预约 -> 预约转订单 -> 收款 -> 完成订单 -> 自动扣减项目耗材
                                              |
                                              -> 取消已完成订单时按原流水回滚库存
```

## 技术栈

| 后端 | 前端 |
| --- | --- |
| Java 17 | Vue 2.6 |
| Spring Boot 3.5.16 | TypeScript 3.6 |
| MyBatis-Plus 3.5.16 | Element UI 2.12 |
| MySQL、Redis、Spring Cache | Vue Router、Vuex、Axios |
| JWT、BCrypt、EasyExcel | Vue CLI 3、Webpack 4、Jest |
| Maven、Lombok | npm |

## 项目结构

```text
后台管理系统/
├─ beauty-console/                         Java 后端父工程
│  ├─ pom.xml
│  ├─ store-common/                        返回结果、异常、JWT、公共上下文
│  ├─ store-pojo/                          DTO、VO、Entity、Enum
│  └─ store-server/                        Controller、Service、Mapper、配置和启动类
│     └─ src/main/resources/
│        ├─ application.yml                后端配置
│        ├─ mapper/                        MyBatis XML
│        └─ sql/                           基础建表、增量补丁和测试数据
├─ beauty-console-web/                     Vue 前端
├─ README.md                               项目说明、启动与排错
└─ 接口文档.md                             完整接口字段与示例
```

## 运行要求

启动前请准备：

- JDK 17，`JAVA_HOME` 必须指向 JDK 17
- Maven 3.8+
- MySQL 8.x
- Redis
- Node.js 与 npm；当前 npm 脚本已加入旧 Webpack 在新版 Node.js 下需要的 OpenSSL 兼容参数

检查本机环境：

```powershell
java -version
mvn -version
node -v
npm -v
```

`mvn -version` 显示的 Java 版本也必须是 17。

## 数据库与 Redis

默认开发配置位于 [`application.yml`](./beauty-console/store-server/src/main/resources/application.yml)：

| 项目 | 默认值 | 环境变量 |
| --- | --- | --- |
| 后端端口 | `8080` | `STORE_PORT` |
| MySQL 主机 | `localhost` | `STORE_DB_HOST` |
| MySQL 端口 | `3306` | `STORE_DB_PORT` |
| 数据库 | `db_platform` | `STORE_DB_DATABASE` |
| MySQL 用户 | `root` | `STORE_DB_USERNAME` |
| MySQL 密码 | `12345678` | `STORE_DB_PASSWORD` |
| Redis 主机 | `localhost` | `STORE_REDIS_HOST` |
| Redis 端口 | `6379` | `STORE_REDIS_PORT` |
| Redis 库 | `0` | `STORE_REDIS_DATABASE` |
| Redis 密码 | 空 | `STORE_REDIS_PASSWORD` |
| JWT 密钥 | `itcast` | `STORE_JWT_ADMIN_SECRET_KEY` |
| JWT 有效期 | `7200000` 毫秒 | `STORE_JWT_ADMIN_TTL` |
| Token 请求头 | `token` | `STORE_JWT_ADMIN_TOKEN_NAME` |
| 演示只读模式 | `false` | `STORE_DEMO_READ_ONLY` |

以上密码和 JWT 密钥只是本地开发默认值，部署到其他环境时必须通过环境变量覆盖。

### SQL 脚本

SQL 位于 [`resources/sql`](./beauty-console/store-server/src/main/resources/sql)：

| 文件 | 用途 |
| --- | --- |
| `schema.sql` | 早期基础表结构，只能用于空数据库基线 |
| `patch_order_columns.sql` | 补充订单欠款和订单明细员工字段 |
| `patch_role_permissions.sql` | 将旧角色结构升级为六类角色 |
| `patch_permission_points.sql` | 权限点、角色权限和用户权限表 |
| `patch_inventory_consumables.sql` | 美容门店耗材样例数据 |
| `patch_service_project_inventory.sql` | 项目耗材关系表和样例关系 |
| `patch_audit_and_unique_numbers.sql` | 审计表和业务编号唯一索引 |
| `patch_test_role_accounts.sql` | 六类本地测试账号 |
| `seed_100_rows.sql` | 较完整的本地联调数据 |

当前仓库保留的是“旧基线 + 增量补丁”迁移历史，并非 Flyway/Liquibase 一键迁移链。已有数据库不要重复执行 `schema.sql`，升级时只执行尚未应用的补丁。`schema.sql` 仍使用旧的 `role_code`，而新代码和角色补丁使用 `role_id`，全新环境需要先合并最新表结构或完成该字段迁移，不能直接把全部 SQL 批量执行。

## 启动后端

在项目根目录打开 PowerShell：

```powershell
Set-Location .\beauty-console
mvn -pl store-server -am install "-DskipTests"

Set-Location .\store-server
mvn org.springframework.boot:spring-boot-maven-plugin:3.5.16:run
```

后端地址：`http://localhost:8080`

也可以在 IntelliJ IDEA 中导入 `beauty-console/pom.xml`，将 Project SDK 和 Maven Runner JRE 都设为 JDK 17，重新加载 Maven 后运行：

```text
com.wkr.storeserver.StoreServerApplication
```

## 启动前端

另开一个 PowerShell 窗口：

```powershell
Set-Location .\beauty-console-web
npm ci
npm run serve
```

前端地址：`http://localhost:8888`

开发环境会把 `/api` 代理到 `.env.development` 中的 `VUE_APP_URL`，当前默认值为 `http://localhost:8080`。应先启动后端，再打开前端。

## 默认账号

执行 `patch_test_role_accounts.sql` 或完整测试种子数据后，可使用以下账号。默认密码均为 `123456`。

| 账号 | 角色 |
| --- | --- |
| `admin` | 超级管理员 |
| `manager` | 店长 |
| `staff` | 普通员工 |
| `inventory` | 库存管理员 |
| `finance` | 财务/收银 |
| `readonly` | 只读 |

## 接口快速说明

普通接口统一响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {}
}
```

分页接口的 `data` 包含 `total` 和 `records`。`code = 1` 表示成功，`code = 0` 表示业务失败。

登录接口不需要 Token：

```powershell
curl.exe -X POST "http://localhost:8080/admin/users/login" `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","password":"123456"}'
```

登录响应的 `data.token` 是后续请求凭证，请通过 `token` 请求头发送：

```http
token: Bearer <token>
```

| 资源 | 基础路径 | 额外操作 |
| --- | --- | --- |
| 用户 | `/admin/users` | 登录、状态、用户权限 |
| 权限点 | `/admin/permissions` | 查询全部权限点 |
| 员工 | `/admin/staff-members` | 状态管理 |
| 客户 | `/admin/customers` | — |
| 服务项目 | `/admin/service-projects` | 上下架 |
| 项目耗材 | `/admin/service-project-inventories` | — |
| 预约 | `/admin/appointments` | 确认、完成、取消 |
| 预约明细 | `/admin/appointment-items` | — |
| 服务订单 | `/admin/service-orders` | 预约转订单、完成、取消 |
| 订单明细 | `/admin/service-order-items` | — |
| 收款流水 | `/admin/payment-records` | 作废 |
| 库存物品 | `/admin/inventory-skus` | 状态、Excel 导出 |
| 库存流水 | `/admin/inventory-stock-logs` | 入库、出库、盘点 |

请求字段、响应字段、枚举值、分页参数和完整示例见 [接口文档.md](./接口文档.md)。

## 核心业务规则

- 预约状态遵循“待确认 -> 已确认 -> 已完成”，待确认或已确认状态可以取消；非法重复流转会返回业务错误。
- 从预约生成订单时会复制预约明细、汇总金额并生成唯一业务编号。
- 新增或作废收款会锁定订单行，重新计算已付、欠款和支付状态，禁止超收。
- 完成订单时按启用的项目耗材关系扣库存：`订单项目数量 × 单次耗材数量`。
- 库存变更会锁定或条件更新库存记录，库存不足时整个事务失败，不产生负库存。
- 取消已完成订单时，会按原始出库流水恢复库存。
- 客户、员工、项目、库存、预约和订单被业务数据引用时禁止直接删除。
- 预约号、订单号和收款号分别使用 `APT`、`ORD`、`PAY` 前缀，并由数据库唯一索引兜底。
- 关键新增、修改、删除、状态变更和收款操作写入 `operation_audit_log`。

## 测试与构建

后端测试：

```powershell
Set-Location .\beauty-console
mvn -pl store-server -am test
```

真实 MySQL + Redis 冒烟测试默认不启用：

```powershell
mvn -pl store-server -am "-Dstore.real-it=true" "-Dtest=AdminApiRealIntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

前端检查：

```powershell
Set-Location .\beauty-console-web
npm run lint
npm run test:unit -- --runInBand
npm run build
```

最近一次本地验证：

- 后端：共 52 项测试，51 项通过，1 项真实环境测试按配置跳过。
- 前端生产构建成功，仅有旧依赖栈和资源体积警告。
- 前端 lint 当前有 11 个既有错误，主要来自生成的 `src/styles/icon/iconfont.js`。
- 前端单测 2 个套件中 1 个通过；Breadcrumb 用例因缺少 `$route.matched` mock 失败。

## 常见问题

### IntelliJ 提示 Java 文件不在模块源目录或运行时报 `ClassNotFoundException`

1. 使用 IntelliJ 打开或导入 `beauty-console/pom.xml`，不要只把某个 `.java` 文件作为普通目录打开。
2. 在 Maven 工具窗口执行 **Reload All Maven Projects**。
3. 确认 `store-server/src/main/java` 被标记为 **Sources Root**。
4. 删除旧的启动配置，再从 `StoreServerApplication` 左侧绿色箭头重新创建。
5. 确认 Project SDK、Module SDK 和 Maven Runner JRE 都是 JDK 17。

### 前端页面打不开或接口报错

- 确认地址是 `http://localhost:8888`，不是后端的 `8080`。
- `Cannot find module ... @vue/cli-service` 表示 `node_modules` 不完整，在前端目录重新执行 `npm ci`。
- `/api` 请求显示 `ECONNREFUSED` 表示前端已启动，但 `http://localhost:8080` 的后端未启动。
- 修改 `.env.development` 的 `VUE_APP_URL` 后需要重启 `npm run serve`。

### 后端连接失败

- `Communications link failure`：MySQL 未启动、端口错误或数据库不存在。
- `Access denied for user`：检查 `STORE_DB_USERNAME` 和 `STORE_DB_PASSWORD`。
- Redis 相关异常：确认 Redis 已监听配置端口；当前缓存没有 Redis 不可用时的完整降级策略。
- 端口占用可执行 `Get-NetTCPConnection -LocalPort 8080` 或 `Get-NetTCPConnection -LocalPort 8888` 排查。

### PowerShell 和 Maven 参数

- PowerShell 下建议将 Maven 的 `-D...` 参数整体加双引号。
- 多模块构建使用 `-pl store-server -am`，避免缺失 `store-common` 或 `store-pojo`。
- 指定单个测试并使用 `-am` 时，加上 `"-Dsurefire.failIfNoSpecifiedTests=false"`。
- Windows 上 Git 的 LF/CRLF 提示通常是换行符转换提醒，不是编译错误。

## 当前状态与后续计划

主要 CRUD、登录鉴权、权限点、预约转订单、收款反写、库存流水、项目耗材自动扣减、审计和删除保护已经实现。后续仍建议处理：

- 将基础建表和全部补丁合并成可重复执行的当前版本初始化方案，并接入 Flyway 或 Liquibase。
- 修复前端 Breadcrumb 单测和生成图标文件的 lint 配置。
- 将真实 MySQL + Redis 集成测试和前端浏览器 E2E 纳入 CI。
- 为 Redis 不可用场景增加降级、监控和告警。
- 增加 Excel 批量导入、库存消耗、低库存预警和员工业绩报表。

## 文档说明

原 `项目介绍.md` 的架构、业务流程和完成情况，以及 `问题清单.md` 中仍有效的事项，已合并到本 README。接口字段数量较多，因此 [接口文档.md](./接口文档.md) 独立保留在根目录。
