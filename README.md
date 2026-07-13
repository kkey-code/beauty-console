# 门店后台管理系统

项目包含 Java 后端和 Vue 前端：

```text
后台管理系统/
├─ beauty-console/       Java 17 + Spring Boot + Maven
└─ beauty-console-web/   Vue 2 + TypeScript
```

## 启动后端

首次运行或本地 Maven 仓库没有内部模块时，先安装依赖模块：

```powershell
cd beauty-console
mvn -pl store-server -am install -DskipTests
```

然后从启动模块运行 Spring Boot：

```powershell
cd store-server
mvn org.springframework.boot:spring-boot-maven-plugin:3.5.16:run
```

后端地址：`http://localhost:8080`

## 启动前端

```powershell
cd beauty-console-web
npm ci
npm run serve
```

前端地址：`http://localhost:8888`

前端已通过 `.npmrc` 固定使用官方 npm 源，并在 npm 脚本中兼容 Node 24 与旧 Webpack 4。

## 验证

后端测试：

```powershell
cd beauty-console
mvn -pl store-server -am test
```

前端构建：

```powershell
cd beauty-console-web
npm run build
```

- 后端配置：`beauty-console/store-server/src/main/resources/application.yml`
- 初始化 SQL：`beauty-console/store-server/src/main/resources/sql/schema.sql`
- 默认端口：后端 `8080`，前端 `8888`
