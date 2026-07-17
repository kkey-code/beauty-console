# 门店后台管理系统

一个面向美容门店经营场景的前后端分离后台系统，覆盖账号权限、员工、客户、服务项目、预约、订单、收款和库存耗材等业务。

- 前端：Vue 2 + TypeScript + Element UI；本地开发端口为 `8888`，Docker 部署端口为 `80`
- 后端：Java 17 + Spring Boot 3.5.16 + MyBatis-Plus，默认运行在 `http://localhost:8080`
- 数据：MySQL + Redis
- 接口：统一使用 `/admin` 前缀，Knife4j 地址为 `http://localhost:8080/doc.html`，详细字段见 [接口文档](./接口文档.md)

## 功能概览

| 模块 | 主要能力 |
| --- | --- |
| 账号与权限 | 一名员工一个账号、六类角色默认权限、账号级权限覆盖、账号启停用 |
| 员工与客户 | 员工档案、客户资料、等级和来源维护、普通员工本人数据范围 |
| 服务项目 | 项目维护、上下架、项目与库存耗材关系配置 |
| 预约 | 预约及明细维护、确认、完成、取消、预约转订单 |
| 服务订单 | 订单及明细维护、金额校验、完成、取消、删除保护 |
| 收款 | 收款流水、订单金额回写、并发防超收、收款作废 |
| 库存 | SKU、入库、出库、盘点、库存流水、Excel 导出 |
| 安全与审计 | BCrypt 密码、JWT 鉴权、接口权限、关键操作审计 |

核心业务链路：

```text
预约 -> 确认预约 -> 预约转订单 -> 收款 -> 完成订单 -> 自动扣减项目耗材

预约转订单前，在“预约管理”行操作中点击“项目明细”，按名称选择服务项目并添加；服务价格和时长会自动回填，明细保存后可在弹窗内直接转为订单。该流程全部在客户端完成，不需要操作 `doc.html`。
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
| Knife4j、Redisson、Bean Validation | Nginx |
| Maven、Lombok | npm、Docker Compose |

## 项目结构

```text
后台管理系统/
├─ beauty-console/                         Java 后端父工程
│  ├─ Dockerfile                           后端镜像构建文件
│  ├─ pom.xml
│  ├─ store-common/                        返回结果、异常、JWT、公共上下文
│  ├─ store-pojo/                          DTO、VO、Entity、Enum
│  └─ store-server/                        Controller、Service、Mapper、配置和启动类
│     └─ src/main/resources/
│        ├─ application.yml                公共配置与默认环境
│        ├─ application-dev.yml            本地开发配置
│        ├─ application-prod.yml           Docker/生产配置
│        ├─ mapper/                        MyBatis XML
│        └─ sql/                           基础建表、增量补丁和测试数据
├─ beauty-console-web/                     Vue 前端、Dockerfile 与 Nginx 配置
├─ docker-compose.yml                      MySQL、Redis、后端、前端一键编排
├─ .env.example                            Docker 环境变量示例
├─ README.md                               项目说明、启动与排错
└─ 接口文档.md                             完整接口字段与示例
```

## 运行要求

推荐使用 Docker Compose 一键部署，只需要：

- Docker Desktop（Windows 下使用 WSL 2 后端）
- Docker Compose v2

如需使用 IDEA 进行本地开发，则需要：

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

服务器增量更新使用 `resources/sql/migration/V*.sql`。`deploy-update.ps1` 会将迁移随镜像打包，服务器端按文件名记录到 `schema_migration`，已经成功执行过的迁移不会重复执行。

## Docker Compose 一键部署（推荐）

Docker Compose 会同时启动 MySQL 8、Redis、Spring Boot 后端以及 Vue + Nginx 前端。Docker 环境通过 `SPRING_PROFILES_ACTIVE=prod` 使用 `application-prod.yml`，MySQL 与 Redis 数据分别保存在命名卷 `mysql-data` 和 `redis-data` 中。

首次部署前，在项目根目录复制环境变量模板并修改密码和 JWT 密钥：

```powershell
Set-Location "D:\_study\java\后台管理系统"
Copy-Item .env.example .env
notepad .env
```

首次部署或源码更新后重新部署：

```powershell
docker compose up -d --build
```

必须使用 `--build` 才会将当前前后端源码重新构建进镜像；只执行 `docker compose up -d` 可能继续运行旧镜像。

查看四个服务的状态：

```powershell
docker compose ps
```

正常情况下 `mysql`、`redis`、`app` 和 `web` 均应显示 `healthy`。服务入口如下：

| 服务 | 地址 |
| --- | --- |
| 管理系统 | `http://localhost` |
| 后端接口 | `http://localhost:8080` |
| Knife4j | `http://localhost:8080/doc.html` |
| 健康检查 | `http://localhost:8080/actuator/health` |

部署到云服务器后，将 `localhost` 替换为服务器公网 IP，并在安全组中按需开放 `80` 和 `8080`。生产环境不建议公开 MySQL、Redis 端口，并应根据需要关闭接口文档。

常用运维命令：

```powershell
# 查看日志
docker compose logs -f app
docker compose logs -f web

# 停止、再次启动
docker compose stop
docker compose start

# 删除容器和网络，保留持久化数据
docker compose down

# 删除容器、网络以及 MySQL/Redis 持久化数据（谨慎使用）
docker compose down -v
```

Docker 部署模式下不要同时启动 IDEA 后端或 `npm run serve`：IDEA 后端会与容器争用 `8080`，而 `8888` 仅用于前端开发服务器，不是部署端口。

### 云服务器一键增量更新

在 Windows 项目根目录执行，其中 `<SERVER_HOST>` 替换为服务器域名或 IP：

```powershell
.\deploy-update.ps1 -Server "<SERVER_HOST>"
```

脚本会依次构建前后端镜像、生成带 SHA-256 清单的版本包、上传到指定服务器、备份 MySQL、执行尚未应用的 `V*.sql`、替换应用容器并做健康检查。失败时会恢复旧应用镜像，并输出数据库备份路径。使用 SSH 密钥后整条命令无需输入密码；仍使用密码登录时，`scp` 和 `ssh` 阶段会正常要求输入服务器密码。

只生成更新包、不上传服务器：

```powershell
.\deploy-update.ps1 -Server "<SERVER_HOST>" -PackageOnly
```

### 云服务器演示数据与乱码修复

云服务器使用 Ubuntu 24.04、Docker Compose v2，公网入口为 `http://<SERVER_HOST>`。服务器版 Compose 只向公网映射 `80`，后端、MySQL 和 Redis 仅在 Docker 内部网络访问。

首次初始化只包含表结构和六个权限测试账号，不会自动导入完整业务演示数据。应用启动后会为没有账号的员工自动补齐登录账号。如果页面中的中文姓名显示为 `æµ...`，说明初始化 SQL 被 MySQL 客户端按错误字符集导入；这不是浏览器编码问题。使用本地文件 `deploy-artifacts/beauty-console-data-fix.tar.gz` 可以补齐演示数据并修复前六名员工姓名。

该修复会临时备份并恢复 `sys_user`，因此不会覆盖管理员现有密码，也不会重新启用已停用的测试账号。先在 Windows PowerShell 上传：

```powershell
scp "D:\_study\java\后台管理系统\deploy-artifacts\beauty-console-data-fix.tar.gz" ubuntu@<SERVER_HOST>:/home/ubuntu/
```

再登录服务器执行：

```bash
cd ~/beauty-console-deploy
tar -xzf ~/beauty-console-data-fix.tar.gz
sudo docker exec beauty-console-mysql-1 sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysqldump --single-transaction --user="$MYSQL_USER" "$MYSQL_DATABASE"' > ~/db-before-data-fix.sql
sudo docker cp seed_100_rows.sql beauty-console-mysql-1:/tmp/seed_100_rows.sql
sudo docker cp server-seed-wrapper.sql beauty-console-mysql-1:/tmp/server-seed-wrapper.sql
sudo docker exec beauty-console-mysql-1 sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql --default-character-set=utf8mb4 --user="$MYSQL_USER" "$MYSQL_DATABASE" < /tmp/server-seed-wrapper.sql'
sudo docker exec beauty-console-redis-1 sh -c 'redis-cli -a "$REDIS_PASSWORD" FLUSHDB'
sudo docker restart beauty-console-app-1
sudo docker compose -f docker-compose.server.yml ps
```

导入结果应显示员工、客户、项目、库存、预约、订单和收款各 100 条，前六名员工显示正常中文。后端重启后会为缺少账号的员工生成 `emp+员工ID` 账号，初始密码为 `123456`。刷新浏览器前端即可看到数据；若使用 SSH 隧道，访问 `http://localhost:8088`。

仅修改前端页面时，可以上传增量前端镜像，无需重启后端、MySQL 和 Redis。以客户档案菜单图标修复包为例，先在 Windows PowerShell 上传：

```powershell
scp "D:\_study\java\后台管理系统\deploy-artifacts\beauty-console-web-icon-fix.tar.gz" ubuntu@<SERVER_HOST>:/home/ubuntu/
```

再在服务器替换前端容器：

```bash
cd ~/beauty-console-deploy
tar -xzf ~/beauty-console-web-icon-fix.tar.gz
sudo docker load -i beauty-console-web-icon-fix.tar
sudo docker compose -f docker-compose.server.yml up -d --no-deps --force-recreate web
sudo docker compose -f docker-compose.server.yml ps
```

更新完成后在浏览器按 `Ctrl+F5` 强制刷新。如果 PWA Service Worker 仍缓存旧页面，在浏览器开发者工具的 Application / Service Workers 中执行 Unregister 后重新打开页面。

## 本地开发：启动后端

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

## 本地开发：启动前端

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
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

分页接口的 `data` 包含 `total` 和 `records`。当前业务码设计为：`200` 成功、`400` 参数错误、`401` 未登录、`403` 无权限、`409` 业务冲突、`429` 请求过于频繁、`500` 系统错误。HTTP 状态码与响应体中的 `code` 保持一致，参数校验信息放在 `message` 字段中。

全局异常处理至少覆盖参数校验异常、业务异常、限流异常和未预期系统异常。下单接口使用基于 Redis/Redisson 的分布式令牌桶限流，同一用户默认每分钟最多创建 10 次订单，超过限制返回 HTTP 429。

新增订单或从预约生成订单前，需要先调用 `POST /admin/service-orders/idempotency-token` 获取 UUID，并通过 `Idempotency-Key` 请求头提交。同一用户使用同一 UUID 并发或重试只会生成一个订单；处理中或无效令牌返回 HTTP 409，成功重试返回第一次生成的订单 ID。

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
| 控制台 | `/admin/dashboard` | 聚合经营概览、手动刷新缓存 |
| 用户 | `/admin/users` | 登录、状态、用户权限 |
| 权限点 | `/admin/permissions` | 查询全部权限点 |
| 员工 | `/admin/staff-members` | 状态管理 |
| 客户 | `/admin/customers` | — |
| 服务项目 | `/admin/service-projects` | 上下架 |
| 项目耗材 | `/admin/service-project-inventories` | — |
| 预约 | `/admin/appointments` | 确认、完成、取消 |
| 预约明细 | `/admin/appointment-items` | — |
| 服务订单 | `/admin/service-orders` | 幂等令牌、预约转订单、完成、取消 |
| 订单明细 | `/admin/service-order-items` | — |
| 收款流水 | `/admin/payment-records` | 作废 |
| 库存物品 | `/admin/inventory-skus` | 状态、Excel 导出 |
| 库存流水 | `/admin/inventory-stock-logs` | 入库、出库、盘点 |

请求字段、响应字段、枚举值、分页参数和完整示例见 [接口文档.md](./接口文档.md)。

## 核心业务规则

- 预约状态遵循“待确认 -> 已确认 -> 已完成”，待确认或已确认状态可以取消；非法重复流转会返回业务错误。
- 从预约生成订单时会复制预约明细、汇总金额并生成唯一业务编号。
- 下单使用 Redis 幂等状态机和数据库唯一索引双重保护，同一 UUID 只产生一个订单。
- 新增或作废收款会锁定订单行，重新计算已付、欠款和支付状态，禁止超收。
- 完成订单时按启用的项目耗材关系扣库存：`订单项目数量 × 单次耗材数量`。
- 库存变更会锁定或条件更新库存记录，库存不足时整个事务失败，不产生负库存。
- 取消已完成订单时，会按原始出库流水恢复库存。
- 客户、员工、项目、库存、预约和订单被业务数据引用时禁止直接删除。
- 预约号、订单号和收款号分别使用 `APT`、`ORD`、`PAY` 前缀，并由数据库唯一索引兜底。
- 关键新增、修改、删除、状态变更和收款操作写入 `operation_audit_log`。

## 页面性能优化

- 控制台原来进入一次会并发请求客户、预约、订单、库存等 7 个接口；现在改为 `GET /admin/dashboard/overview` 一次聚合返回，并按“用户 ID + 角色 ID”使用 Redis 缓存 30 秒。
- 项目耗材分页原来会对当前页每条关系分别查询项目和耗材，10 条数据约执行 21 次 SQL；现在收集关联 ID 后使用 `listByIds` 批量查询，稳定在约 3 次 SQL。
- 列表页面切换时会取消上一页尚未结束的 Axios 请求，并使用请求序号阻止旧响应覆盖新页面；查询、刷新和分页操作在加载期间不可重复触发。
- Axios 超时从 10 分钟调整为 30 秒，避免网络故障时页面长时间保持“卡死”状态。
- Nginx 对 JS、CSS、JSON、SVG 等文本资源启用 gzip，并为带哈希的静态文件设置 30 天浏览器缓存；前后端代理使用 keep-alive。
- 浏览器默认标题、路由标题和 PWA 应用名称统一为“美容门店后台”，清除脚手架遗留的“苍穹外卖”名称。
- 后端对超过 JavaScript 安全整数范围的 `Long` 主键按字符串返回，避免雪花 ID 在浏览器中被四舍五入后出现“用户不存在”或误操作其他记录；安全范围内的既有小 ID 仍保持数字格式。
- 前端业务编号按模块显示前缀，例如 `CUS-`、`EMP-`、`APPT-`、`ORD-`、`SKU-`、`ACC-`；数据库和接口关联仍使用原始 `Long` 主键，避免修改外键结构。
- 每个账号必须关联一名员工，同一员工只能有一个账号。新增员工时同步创建账号，账号可留空自动生成，默认密码为 `123456`；新账号继承所选角色的默认权限，账号级权限只负责例外覆盖。
- 菜单由角色上限和查看权限共同控制；进入业务页面后保留新增、编辑、删除等功能按钮，真正提交前再校验动作权限。无权限时明确提示并停止请求，后端继续以 403 做最终安全校验。
- “员工账号与角色权限”是敏感管理菜单，只对超级管理员和店长开放；普通角色不能通过自定义权限突破角色上限获得账号管理能力。
- 普通员工的数据范围按绑定的员工 ID 过滤：只显示自己建档或参与服务的客户、自己主负责或被项目分配的预约、自己参与的订单，控制台统计与列表使用相同规则。
- Redis 只适合缓存允许短暂不一致、重复读取频繁的数据，不会自动加速所有 SQL。涉及分页筛选、实时库存和写操作时，仍应优先优化 SQL、索引和请求生命周期。

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

- 后端：共 85 项测试，84 项通过，1 项外部真实环境测试按配置跳过。
- 前端生产构建成功，仅有旧依赖栈和资源体积警告。
- TypeScript `tsc --noEmit` 检查通过。
- Docker 前后端镜像构建成功，四个容器健康，首页、登录、健康检查和 Knife4j 均已完成真实请求验证。
- 超级管理员、店长、普通员工、库存、财务、只读六类账号完成真实接口矩阵验证：允许请求为 200、禁止请求为 403，没有权限相关的 500；只读账号真实页面验证了按钮保留和提交拦截。
- k6 以每秒 20 个请求持续 30 秒压测订单列表，优化后平均响应由 40.63ms 降至 16.20ms，P95 由 78.02ms 降至 25.20ms；错误率均为 0。
- 本地 Docker 实测控制台强制刷新约 128.2ms，Redis 缓存请求 10 次平均约 18ms；项目耗材分页 10 次平均约 38.5ms。
- 页面级验证完成“项目耗材 -> 库存耗材 -> 客户档案 -> 项目耗材”来回切换，最终表格正常显示且浏览器控制台无错误。

## 常见问题

### IntelliJ 提示 Java 文件不在模块源目录或运行时报 `ClassNotFoundException`

1. 使用 IntelliJ 打开或导入 `beauty-console/pom.xml`，不要只把某个 `.java` 文件作为普通目录打开。
2. 在 Maven 工具窗口执行 **Reload All Maven Projects**。
3. 确认 `store-server/src/main/java` 被标记为 **Sources Root**。
4. 删除旧的启动配置，再从 `StoreServerApplication` 左侧绿色箭头重新创建。
5. 确认 Project SDK、Module SDK 和 Maven Runner JRE 都是 JDK 17。

### 前端页面打不开或接口报错

- Docker 部署访问 `http://localhost`；本地前端开发才访问 `http://localhost:8888`；`8080` 是后端端口。
- Docker 前端能打开但登录返回 `502`，表示 Nginx 无法连接 `app` 容器。执行 `docker compose ps`，并使用 `docker compose logs -f app` 检查后端是否健康。
- 修改源码后页面仍表现为旧版本时，执行 `docker compose up -d --build` 重建镜像，并在浏览器中强制刷新。
- `Cannot find module ... @vue/cli-service` 表示 `node_modules` 不完整，在前端目录重新执行 `npm ci`。
- `/api` 请求显示 `ECONNREFUSED` 表示前端已启动，但 `http://localhost:8080` 的后端未启动。
- 修改 `.env.development` 的 `VUE_APP_URL` 后需要重启 `npm run serve`。

### 店长有工作台权限但接口返回 403

如果登录响应包含 `dashboard:view`，但 `GET /admin/dashboard/overview` 仍提示“无权限访问该接口”，通常是旧数据库中的权限路径仍未升级。版本化迁移 `V20260716_02__dashboard_permission_path.sql` 会把规则更新为 `GET /admin/dashboard/**`，应用启动兼容检查也会自动修复。手工修改过数据库后需要重启后端，以清除旧权限规则缓存。

### 后端连接失败

- `Communications link failure`：MySQL 未启动、端口错误或数据库不存在。
- `Access denied for user`：检查 `STORE_DB_USERNAME` 和 `STORE_DB_PASSWORD`。
- Redis 相关异常：确认 Redis 已监听配置端口；当前缓存没有 Redis 不可用时的完整降级策略。
- Docker 后端启动时报 `Port 8080 was already in use` 时，先停止 IDEA 中运行的后端；开发模式和部署模式不能同时占用 `8080`。
- 端口占用可执行 `Get-NetTCPConnection -LocalPort 80`、`Get-NetTCPConnection -LocalPort 8080` 或 `Get-NetTCPConnection -LocalPort 8888` 排查。

### PowerShell 和 Maven 参数

- PowerShell 下建议将 Maven 的 `-D...` 参数整体加双引号。
- 多模块构建使用 `-pl store-server -am`，避免缺失 `store-common` 或 `store-pojo`。
- 指定单个测试并使用 `-am` 时，加上 `"-Dsurefire.failIfNoSpecifiedTests=false"`。
- Windows 上 Git 的 LF/CRLF 提示通常是换行符转换提醒，不是编译错误。

## 当前状态与后续计划

主要 CRUD、登录鉴权、权限点、预约转订单、下单幂等、分布式限流、收款反写、库存流水、项目耗材自动扣减、审计、删除保护、订单列表 N+1 优化、控制台聚合缓存和项目耗材批量查询已经实现。后续仍建议处理：

- 将基础建表和全部补丁合并成可重复执行的当前版本初始化方案，并接入 Flyway 或 Liquibase。
- 修复前端 Breadcrumb 单测和生成图标文件的 lint 配置。
- 将真实 MySQL + Redis 集成测试和前端浏览器 E2E 纳入 CI。
- 为 Redis 不可用场景增加降级、监控和告警。
- 增加 Excel 批量导入、库存消耗、低库存预警和员工业绩报表。

## 文档说明

原 `项目介绍.md` 的架构、业务流程和完成情况，以及 `问题清单.md` 中仍有效的事项，已合并到本 README。接口字段数量较多，因此 [接口文档.md](./接口文档.md) 独立保留在根目录。

- [压测与优化对比报告.md](./压测与优化对比报告.md)：优化内容、测试结果和可复现的验证口径。
