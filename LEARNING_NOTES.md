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

状态：尚未开始。

本阶段完成后补充以下内容：

- `index.html → main.ts → App.vue` 的启动关系。
- 首页和路由的实现过程。
- `npm run dev` 与 `npm run build` 的验证结果。
- 正常场景和错误场景测试。

