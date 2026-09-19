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
- 数据库是否能够从新项目正常连接：阶段 4 已验证，现有 `health` 库可查询 `department` 表。
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

## 阶段 4：数据库与部门管理（列表查询）

状态：已完成部门列表查询、新增、修改和删除。

### 旧项目分析

- 部门表：`department`。
- 主键：`id`（自增）；层级字段：`parent_id`；展示与筛选字段：`dept_name`、`dept_code`；排序和状态字段：`sort_order`、`status`。
- 调用链：`DepartmentController → DepartmentService → DepartmentMapper → department`。
- 旧项目还包含树查询和增删改。已实现新增 `POST /department/create`、修改 `PUT /department/update`、删除 `DELETE /department/delete/{id}`。名称和编码必填。
- 旧前端状态约定：`status === 0` 表示正常，其他值表示停用。

### 新项目实现

- `HealthApi/pom.xml` 增加 SQL Server JDBC 驱动和 MyBatis-Plus Spring Boot 3 starter。
- 数据库地址、账号、密码直接写在 `HealthApi/src/main/resources/application.yml`。
- 服务端口是 `8081`。不需要环境变量，也不需要额外的 yml。
- `DepartmentMapper.findList` 使用参数化 SQL。只有关键字不为空时才加 `LIKE` 条件，并用 `CONCAT` 拼接，避免 SQL Server 把空参数当成 varbinary。
- `GET /health/department/list?keyword=...` 返回部门数组。
- 前端：`src/api/department.ts`、`src/views/DepartmentView.vue`、路由 `/departments`。
- `DepartmentService.create` 校验名称、编码非空及长度，默认状态 `0`（正常），然后调用 MyBatis-Plus `insert`。
- `DepartmentService.update` 先按 id 查出原记录，不存在则拒绝；再改名称和编码，调用 `updateById`，避免把未提交的字段覆盖成空。
- `DepartmentService.delete` 先确认记录存在，再 `deleteById`。前端删除前会 `confirm`。
- 非法输入抛出 `IllegalArgumentException`，由 `RestExceptionHandler` 返回 HTTP 400 和 `{ "message": "..." }`。
- 表 `department` 中 `dept_name`、`dept_code` 为 NOT NULL；`create_time` 有数据库默认值，插入时不必手写。

### 验证

```bash
mvn -q test -f HealthApi/pom.xml
npm --prefix HealthWeb run type-check
npm --prefix HealthWeb run build
```

结果：后端列表、新增、修改、删除相关测试通过；前端 type-check 和 build 通过。

## 阶段 5：登录（最小可用）

状态：已完成用户名密码登录、退出，并按角色保护部门写操作。角色管理页面尚未开始。

认证是确认你是谁（登录）。授权是确认你能干什么（`SUPER_ADMIN` 才能增删改部门）。

### 旧项目分析

- 用户表：现有 `sys_user`，密码为 BCrypt。
- 认证：Sa-Token，`StpUtil.login(userId)` 生成 Token。
- 前端请求头：`satoken`。

### 新项目实现

- 加入 `sa-token-spring-boot3-starter` 和 `spring-security-crypto`。
- `POST /health/auth/login`、`POST /health/auth/logout`、`GET /health/auth/info`。
- 未登录访问 `/department/**` 返回 401「请先登录」。
- 部门新增/修改/删除需要角色 `SUPER_ADMIN`，否则 403「没有权限执行该操作」。列表只要登录即可。
- 角色来自现有表 `sys_user_role`、`sys_role`。当前库里 `admin` 绑定了 `SUPER_ADMIN`。
- 前端：登录页、localStorage 存 Token 和角色、Axios 自动带 Token、路由守卫、退出。非管理员不显示增删改按钮。

### 验证

```bash
mvn -q test -f HealthApi/pom.xml
npm --prefix HealthWeb run type-check
```

## 阶段 6：岗位列表（第一步）

状态：已完成岗位列表、新增、修改和删除。人员、设备尚未开始。

- 旧项目岗位对应表 `job_type`（工种），接口 `/job-type/list`。
- 新项目：`JobType` / `JobTypeMapper` / `JobTypeService` / `JobTypeController`。
- 前端：`src/api/jobType.ts`、`src/views/JobTypeView.vue`、路由 `/job-types`，需登录。超级管理员才能写。
- 库中现有 12 条工种，例如综采工。风险等级 1/2/3 表示低/中/高。

## 阶段 6：人员列表（第一步）

状态：已完成人员分页查询、新增、修改和删除。设备尚未开始。

- 表 `employee` 约 1000 条，所以列表用 `OFFSET/FETCH` 分页，默认每页 20 条。
- 关联 `department`、`job_type` 带出部门名和岗位名。
- 接口：`GET /employee/list?keyword=&page=&size=`，返回 `{ list, total, page, size }`。
- 前端：`src/api/employee.ts`、`src/views/EmployeeView.vue`、路由 `/employees`，需登录。超级管理员才能写。
- 姓名、工号必填；工号有唯一索引；部门和岗位可选，但必须是现有记录。

## 阶段 6：设备列表（第一步）

状态：已完成设备分页查询、增删改，以及人员与设备绑定、解绑。

- 表 `device` 约 1000 台，`imei` 唯一。当前绑定看 `device_user.is_current = 1`。
- 接口：`GET /device/list?keyword=&page=&size=`，可按 IMEI、姓名、工号搜索。
- 前端：`src/api/device.ts`、`src/views/DeviceView.vue`、路由 `/devices`，需登录。超级管理员才能写。
- IMEI 必须 15 位数字且唯一；已绑定职工的设备不能删除。
- 绑定写入 `device_user`：`POST /device/{id}/bind`，按工号找人。解绑把 `is_current` 设为 0。一台设备、一个职工都只能有一条当前绑定。

## 阶段 7：心率

状态：已完成按工号查询、分页、趋势图，以及写入一条心率。

- 不新建表，读取现有 `v_health_record`。`user_code` 在库里是工号，例如 EMP0001。
- 写入当前月份分区表 `health_record_yyyyMM`。
- 接口：`GET /heart-rate/list`、`GET /heart-rate/trend`、`POST /heart-rate/create`。
- 前端：`src/views/HeartRateView.vue`，ECharts 折线图，最多 200 个点。

真实数据库查询：

- `GET /health/actuator/health`：`{"status":"UP"}`
- `GET /health/department/list`：返回 20 条现有部门，例如综采一队。
- `GET /health/department/list?keyword=采`：返回 4 条。
- `GET /health/department/list?keyword=NO_SUCH_DEPT_XYZ`：返回空数组。

启动后端：

```bash
cd HealthApi
mvn spring-boot:run
```

改数据库账号密码时，只改 `application.yml`。IDEA 里直接启动即可，不用配环境变量。

## 阶段 8：血压（第一步）

状态：已完成按工号查询、分页、趋势图，以及写入一条血压。

- 复用现有 `v_health_record` 视图中的 `blood_pressure_high`（收缩压）和 `blood_pressure_low`（舒张压）字段，不新建表。
- 写入当前月份分区表 `health_record_yyyyMM`，自动化测试使用 Mock，不修改共享数据库。
- 后端调用链：`BloodPressureController → BloodPressureService → BloodPressureMapper → v_health_record / health_record_yyyyMM`。
- 接口：`GET /blood-pressure/list`、`GET /blood-pressure/trend`、`POST /blood-pressure/create`。
- 前端：`src/api/bloodPressure.ts`、`src/views/BloodPressureView.vue`、路由 `/blood-pressure`，页面使用 ECharts 同时绘制收缩压和舒张压。
- 血压页面和接口需要登录；写入接口需要 `SUPER_ADMIN` 角色。
- 写入校验：工号必须存在；收缩压范围为 40 到 300，舒张压范围为 20 到 200，且收缩压必须高于舒张压。

### 验证结果

- `mvn -q test`：通过。
- `npm run type-check`：通过。
- `npm run build`：通过。
- 临时启动当前版本并查询真实数据库：`EMP0001` 列表返回 6510 条血压记录，趋势接口返回真实记录。
- 未登录访问 `/blood-pressure/list`：返回 HTTP 401。
- 验证使用的临时 8082、8083 服务已停止；原有 8081 服务未停止。

## 阶段 8：血氧（第二步）

状态：已完成按工号查询、分页、趋势图，以及写入一条血氧。

- 复用现有 `v_health_record` 视图中的 `blood_oxygen` 字段，单位为百分比，不新建表。
- 写入当前月份分区表 `health_record_yyyyMM`，自动化测试使用 Mock，不修改共享数据库。
- 后端调用链：`BloodOxygenController → BloodOxygenService → BloodOxygenMapper → v_health_record / health_record_yyyyMM`。
- 接口：`GET /blood-oxygen/list`、`GET /blood-oxygen/trend`、`POST /blood-oxygen/create`。
- 前端：`src/api/bloodOxygen.ts`、`src/views/BloodOxygenView.vue`、路由 `/blood-oxygen`，页面使用 ECharts 绘制血氧趋势。
- 血氧页面和接口需要登录；写入接口需要 `SUPER_ADMIN` 角色。
- 写入校验：工号必须存在，血氧范围为 50 到 100。

### 验证结果

- `mvn -q test`：通过。
- `npm run type-check`：通过。
- `npm run build`：通过。
- 临时启动当前版本并查询真实数据库：`EMP0001` 列表返回 3328 条血氧记录，趋势接口返回真实记录。
- 验证使用的临时 8082 服务已停止；原有 8081 服务未停止。

## 阶段 8：体温（第三步）

状态：已完成按工号查询、分页、趋势图，以及写入一条体温。

- 复用现有 `v_health_record` 视图中的 `temperature` 字段；数据库保存实际温度乘以 10 的整数，例如 `367` 表示 `36.7℃`，不新建表。
- 写入当前月份分区表 `health_record_yyyyMM`，读取时转换为一位小数的摄氏温度。
- 后端调用链：`TemperatureController → TemperatureService → TemperatureMapper → v_health_record / health_record_yyyyMM`。
- 接口：`GET /temperature/list`、`GET /temperature/trend`、`POST /temperature/create`。
- 前端：`src/api/temperature.ts`、`src/views/TemperatureView.vue`、路由 `/temperature`，ECharts 展示体温趋势，单位为 `℃`。
- 体温页面和接口需要登录；写入接口需要 `SUPER_ADMIN` 角色。
- 写入校验：工号必须存在，体温范围为 `35.0℃` 到 `42.0℃`；写入时按一位小数四舍五入为数据库整数。

## 阶段 8：压力（第四步）

状态：已完成按工号查询、分页、趋势图，以及写入一条压力指数。

- 复用现有 `v_health_record` 视图中的 `pressure` 字段，不新建表；旧项目约定压力指数范围为 30–100，70 以上表示偏高。
- 写入当前月份分区表 `health_record_yyyyMM`。
- 后端调用链：`PressureController → PressureService → PressureMapper → v_health_record / health_record_yyyyMM`。
- 接口：`GET /pressure/list`、`GET /pressure/trend`、`POST /pressure/create`。
- 前端：`src/api/pressure.ts`、`src/views/PressureView.vue`、路由 `/pressure`，ECharts 展示压力指数趋势。
- 压力页面和接口需要登录；写入接口需要 `SUPER_ADMIN` 角色；写入校验范围为 30–100。

## 阶段 8：睡眠（第五步）

状态：已完成按工号查询、分页、趋势图，以及写入一条睡眠记录。

- 复用现有 `v_health_record` 视图中的 `sleep_minutes` 字段，单位为分钟，不新建表。
- 写入当前月份分区表 `health_record_yyyyMM`；查询过滤 1–1439 分钟，避免把无效值或超过 24 小时的占位值当作睡眠记录。
- 后端调用链：`SleepController → SleepService → SleepMapper → v_health_record / health_record_yyyyMM`。
- 接口：`GET /sleep/list`、`GET /sleep/trend`、`POST /sleep/create`。
- 前端：`src/api/sleep.ts`、`src/views/SleepView.vue`、路由 `/sleep`，表格显示分钟和小时，ECharts 以小时展示趋势。
- 睡眠页面和接口需要登录；写入接口需要 `SUPER_ADMIN` 角色；写入校验范围为 1–1439 分钟。

### 三种指标的共同点与差异

- 共同点：都按工号和日期范围查询，后端分页，趋势接口最多返回 200 个点，写入当前月份分区表，并通过登录/管理员权限保护。
- 差异：体温需要在数据库整数和摄氏小数之间换算；压力直接使用 30–100 的指数；睡眠以分钟存储、前端以小时绘图。

### 验证结果

- `mvn -q test -f HealthApi/pom.xml`：通过。
- `npm --prefix HealthWeb run type-check`：通过。
- `npm --prefix HealthWeb run build`：通过（Vite 仅提示现有 ECharts bundle 较大和既有动态导入提示）。
- 自动化测试覆盖正常列表、空列表响应和非法写入错误；测试使用 Mock，不修改共享数据库。

## 阶段 9：阈值配置（第一步）

状态：已完成阈值配置的读取、修改和启停；越界判断与预警生成尚未开始。

### 旧项目分析

- 旧项目的健康阈值统一存放在 `alert_config` 表中，按 `config_type` 区分指标，按 `risk_level` 区分默认、低风险、中风险和高风险配置。
- 阈值字段分为正常范围、低侧预警/中危/高危阈值，以及高侧预警/中危/高危阈值。
- 旧项目的配置接口是 `GET /alert-config/list`、`PUT /alert-config/update` 和 `PUT /alert-config/toggle/{id}`。本阶段沿用这组接口语义，先把配置边界做清楚，后续再把它接入健康数据判断。

### 新项目实现

- 新增 `AlertConfig`、`AlertConfigMapper`、`AlertConfigService` 和 `AlertConfigController`。
- 列表按指标、默认配置、风险级别和 ID 稳定排序；修改只允许改变阈值数值，不允许通过这个接口改动指标、单位和风险级别。
- 服务端校验阈值顺序：低侧为“高危 ≤ 中危 ≤ 预警 ≤ 正常下限”，高侧为“正常上限 ≤ 预警 ≤ 中危 ≤ 高危”。空值、逆序和不存在的 ID 都返回 HTTP 400，不写入数据库。
- `/alert-config/**` 已加入登录和 `SUPER_ADMIN` 写权限规则。普通登录用户可以查看，只有超级管理员可以修改和启停。
- 前端新增 `/alert-config` 页面和 API，显示现有 20 条配置，可编辑数值并启停配置。页面明确提示：当前步骤不会自动生成预警。
- 本次只读取并核对了共享数据库中的 `alert_config` 结构，没有执行建表、种子数据或业务数据写入；自动化测试使用 Mock。

### 验证

```bash
mvn -q test -f HealthApi/pom.xml
npm --prefix HealthWeb run type-check
npm --prefix HealthWeb run build
```

结果：阈值配置 Controller、Service 和权限规则测试通过；前端类型检查和构建通过。提交由用户自行完成。
