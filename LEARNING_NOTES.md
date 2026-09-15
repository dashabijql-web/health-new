# Health 项目学习记录

## 使用方式

每完成一个小功能，在本文档中记录：

1. 功能解决的问题。
2. 旧项目中的入口和调用链。
3. 新项目中的实现思路。
4. 验证命令及结果。
5. 遇到的问题和自己的理解。

## 阶段 0：准备学习环境

### 目录

- 当前学习项目：`/Users/jiangqianli/Documents/code/health`
- 旧项目参考目录：`/Users/jiangqianli/Documents/code/health-old`
- 两个项目是相互独立的同级目录。

### 已确认的开发环境

检查日期：2026-09-15

- Java：21.0.12
- Maven：3.9.9
- Node.js：24.17.0
- npm：11.13.0
- Git：2.54.0

检查命令：

```bash
java -version
mvn -version
node -v
npm -v
git --version
```

### Git 仓库

- 当前仓库分支：`main`
- 远程跟踪分支：`origin/main`
- 已创建适用于 Java、Node.js、Vue、Vite 和 macOS 的 `.gitignore`。
- 不复制旧项目的 `.git`、依赖目录、构建产物、日志和本地敏感配置。

常用检查命令：

```bash
git status
git diff
git log --oneline --decorate -10
```

提交一个学习里程碑：

```bash
git add <本次修改的文件>
git diff --cached
git commit -m "<提交说明>"
git push
```

### 数据库决定

- 新项目复用旧项目现有的 SQL Server 数据库 `health`。
- 数据库是否能够从新项目正常连接：待阶段 4 验证。
- 数据库账号、密码和地址只放在本地环境变量或不提交的本地配置中。
- 未确认影响范围并做好备份前，不执行删库、删表、清空数据或不可逆的表结构变更。
- 自动化测试不得清空或覆盖现有业务数据。
- 需要写入测试数据时，数据必须可识别并可单独清理。

### 阶段 0 完成情况

- [x] 确认 Java、Maven、Node.js、npm 和 Git 可用。
- [x] 确认当前项目和旧项目位于相互独立的目录。
- [x] 初始化当前 Git 仓库并关联远程仓库。
- [x] 创建并提交 `.gitignore`。
- [x] 确定复用现有数据库 `health`。
- [x] 建立学习记录文件。
- [x] 记录常用检查和提交命令。

## 阶段 1：最小 Vue 前端

状态：已完成。

- 新前端项目目录：`HealthWeb`
- 旧前端参考目录：`/Users/jiangqianli/Documents/code/health-old/HealthShow`

### 启动链路

```text
index.html
  → 加载 /src/main.ts
  → 创建 Vue 应用并安装 Vue Router
  → 把 App.vue 挂载到 #app
  → App.vue 通过 RouterView 显示当前路由页面
```

### 当前页面和路由

- `/`：显示“Health 学习版”首页。
- `/about`：显示关于页面，用于验证前端路由切换。
- `App.vue` 提供公共页面外壳和导航。
- `src/router/index.ts` 负责把 URL 路径映射到页面组件。

### 验证结果

- `npm run type-check`：通过。
- `npm run build`：通过，共转换 31 个模块。
- `npm run dev -- --host 127.0.0.1`：成功启动在 `http://127.0.0.1:5173/`。
- 浏览器首页渲染：通过。
- 从首页切换到 `/about`：通过，显示“关于本项目”。
- `npm audit`：0 个已知漏洞。

### 学习确认

- 已理解 `index.html → main.ts → App.vue` 的启动关系。
- 当前阶段没有网络请求或用户输入，暂不添加人为构造的错误场景；后续接口阶段再验证失败处理。

## 阶段 2：最小 Spring Boot 后端

状态：已完成。

- 新后端项目目录：`HealthApi`
- 旧后端参考目录：`/Users/jiangqianli/Documents/code/health-old/HealthData`
- Java 根包：`com.xzkj.health`
- Spring Boot：3.5.16
- 服务端口：8081，避免与旧项目常用端口冲突。
- 应用上下文路径：`/health`

### 当前调用关系

```text
HTTP GET /health/hello
  → Spring DispatcherServlet
  → HelloController.hello()
  → JSON {"message":"Hello from Health API"}
```

### Java 环境提醒

已在用户级 Zsh 登录环境中将 Java 21 配置为默认版本。新打开的终端中，`java`、`javac` 和 Maven 均已验证使用 Java 21。已经打开的旧终端可执行：

```bash
source ~/.zprofile
java -version
mvn -version
```

### 常用命令

```bash
cd /Users/jiangqianli/Documents/code/health/HealthApi
mvn test
mvn spring-boot:run
```

### 验证结果

- `mvn test`：通过，2 个测试无失败。
- `GET http://127.0.0.1:8081/health/hello`：返回预期 JSON。
- `GET http://127.0.0.1:8081/health/actuator/health`：返回 `{"status":"UP"}`。
- `GET http://127.0.0.1:8081/health/missing`：返回 HTTP 404。
- 验证结束后已正常停止后端服务。

### 学习确认

- 已理解 `HealthApplication` 用于启动 Spring Boot 和创建应用容器。
- 已理解 `HelloController` 负责接收 HTTP 请求并返回 JSON。
- 已理解 Actuator 健康端点由自动配置提供，不是自定义 Controller。
- 已理解当前默认健康检查包括磁盘空间、基础 `ping` 和 SSL 证书状态，但尚未检查数据库或 Redis。

## 阶段 3：第一次前后端请求

状态：已完成。

### 调用链

```text
HomeView.vue 点击“调用后端”
  → src/api/hello.ts
  → src/utils/request.ts 中的 Axios 实例
  → 浏览器请求 GET /dev-api/hello
  → Vite 开发服务器代理并改写为 /health/hello
  → http://127.0.0.1:8081/health/hello
  → HealthApi 的 HelloController
  → 页面显示返回的 message
```

### 代理配置

- 浏览器侧基础地址：`/dev-api`
- 后端目标地址：`http://127.0.0.1:8081`
- 路径改写：`/dev-api/hello` → `/health/hello`
- 可以通过本地环境变量 `VITE_API_TARGET` 临时覆盖代理目标，默认无需配置。

### 验证结果

- `npm run type-check`：通过。
- `npm run build`：通过，共转换 89 个模块。
- `npm audit`：0 个已知漏洞。
- 成功场景：页面显示 `Hello from Health API`。
- 失败场景：代理目标不可用时，页面显示“无法连接 Health API，请确认后端已在 8081 端口启动。”
- 为验证失败场景启动的前端服务已停止。
- 用户在 IDEA 中启动的 8081 后端未被停止。

### 学习确认

- 已在浏览器开发者工具的 Network 面板中观察请求 URL、GET 方法、HTTP 200 状态码和 JSON 响应。
- 已理解 Axios `baseURL` 用于统一添加请求前缀。
- 已理解 Vite 代理在开发环境中把 `/dev-api` 请求改写并转发给后端。
- 已理解浏览器只看到发往 Vite 的请求，Vite 到 Spring Boot 的转发发生在开发服务器内部。
- 已理解开发代理可以避免浏览器直接跨域请求后端。
