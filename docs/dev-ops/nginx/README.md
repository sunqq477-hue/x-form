# x-form 本机页面与接口

本机开发默认用 Python 标准库提供静态页面，并把 `/api/` 转发到本机运行的 Spring Boot 服务 `127.0.0.1:8091`。本机不需要安装 Docker；项目页面和后端在电脑上运行，MySQL、Redis 通过 SSH 隧道连接服务器。

## 页面地址

- 管理端：http://127.0.0.1:18080/admin/
- 用户填写端：http://127.0.0.1:18080/user/?code=分享码

创建表单后，管理端会生成可复制的填写链接。

开发阶段的分享链接使用本机地址，只能在当前电脑打开。要让外部用户访问，需要再把页面和后端部署到有公网域名或地址的服务器。

## SSH 隧道映射

| 用途 | 本机地址 | 服务器目标 |
| --- | --- | --- |
| Portainer | 127.0.0.1:54363 | 127.0.0.1:9443 |
| phpMyAdmin | 127.0.0.1:18899 | 127.0.0.1:8899 |
| Redis Commander | 127.0.0.1:18082 | 127.0.0.1:8081 |
| MySQL | 127.0.0.1:13307 | 127.0.0.1:13306 |
| Redis | 127.0.0.1:16380 | 127.0.0.1:16379 |

## 启动步骤

1. 先启动 SSH 隧道。可在 PowerShell 执行 `docs/dev-ops/start-resource-tunnels.ps1`，保持该窗口打开。若已有相同本机端口的隧道运行，请勿重复启动。MySQL 使用本机 `13307`，Redis 使用本机 `16380`；Redis Commander 网页使用 `18081` 或 `18082`，不能拿来当 Redis 服务端口。
2. 在准备启动后端的 PowerShell 窗口设置数据库账号和密码：

   ```powershell
   $env:MYSQL_USER = 'sqq_user'
   $env:MYSQL_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'MySQL password' -AsSecureString)).Password
   $env:REDIS_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Redis password' -AsSecureString)).Password
   ```

   Redis 用户名保持未设置，按服务器的密码认证方式连接；若以后改用 ACL，再设置 `$env:SPRING_DATA_REDIS_USERNAME = 'default'`。密码只保存在当前 PowerShell 进程环境中，不要写入项目文件或提交到 Git。开发配置默认使用服务器数据库 `sqq`。
3. 设置已安装的 JDK 17，并用 Maven 3.9.16 编译启动：

   ```powershell
   $env:JAVA_HOME = 'C:\Users\28112\AppData\Local\Programs\CodexBuildTools\jdk-17'
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   & 'C:\Users\28112\AppData\Local\Programs\CodexBuildTools\maven-3.9.16\bin\mvn.cmd' -pl x-form-app -am package -DskipTests
   & "$env:JAVA_HOME\bin\java.exe" -jar x-form-app/target/x-form-app.jar
   ```

   这个 PowerShell 窗口保持运行。也可以在 VS Code 中用同样的环境变量启动 `cn.xiaofuge.x.Application`。
4. 再开一个 PowerShell 窗口，在本目录运行 `python .\dev_server.py`，保持窗口打开后访问页面地址。本地 Docker Compose 是可选的替代页面服务器；若用它，不要同时运行 `dev_server.py`，因为它们都会占用本机 `18080`。

开发配置默认连接 `sqq` 数据库。首次启动后端时，Spring 会执行仅含 CREATE TABLE IF NOT EXISTS 的 `x-form-app/src/main/resources/db/schema.sql`，创建 `x_form_definition` 和 `x_form_submission` 两张表；不会删除现有数据。这两个表使用 `x_form_` 前缀，避免和其他教程或项目的表重名。

数据库地址、账号和密码可通过 MYSQL_HOST、MYSQL_PORT、MYSQL_DATABASE、MYSQL_USER、MYSQL_PASSWORD 覆盖。Redis 可通过 REDIS_HOST、REDIS_PORT、REDIS_PASSWORD 覆盖；使用 ACL 时可额外设置 `SPRING_DATA_REDIS_USERNAME`。Redis 只缓存公开表单定义；缓存暂时不可用时，查询会回退到 MySQL。

当前 Nginx Compose 配置是可选的本机页面服务器：Nginx 只绑定本机 18080，并代理到本机运行的后端 8091。服务器 Docker 可以留待最终部署时使用；若把页面和后端部署到服务器，需要单独配置服务器端 Compose 和服务间网络。普通本地到服务器的 SSH 本地转发只让本机访问服务器服务，不会让服务器容器反向访问本机后端。

## 接口

- 管理端调用 `GET /api/admin/forms` 查询表单，支持名称/分享码搜索、状态筛选和分页。
- 管理端调用 `POST /api/admin/forms` 创建并发布表单。
- 管理端调用 `GET /api/admin/forms/{id}/submissions` 查询某个表单的填写记录；页面提供内容筛选和 CSV 导出。
- 用户端调用 `GET /api/public/forms/{shareCode}` 读取公开表单。
- 用户端调用 `POST /api/public/forms/{shareCode}/submissions` 提交填写内容。

管理端和用户端的页面行为分别在 `html/assets/admin.js`、`html/assets/user.js`；接口请求统一在 `html/assets/api.js`。创建失败会保留编辑内容并显示后端校验信息；如果后端未启动，会明确提示检查本机 8091 端口。

管理端提供的是浏览器内模拟登录（演示账号 `demo`，密码 `demo123`），只控制页面展示，不会鉴权或保护后端 API。接口请求封装在 `html/assets/api.js`；不要将该开发服务直接暴露到公网。
