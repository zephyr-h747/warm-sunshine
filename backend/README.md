# AI 智能养老社区管理系统（eldercare）

面向养老服务场景的综合性服务平台：健康数据记录、AI 健康咨询（DeepSeek 流式）、健康评测、体检预约、社区活动、积分体系，提供会员端（移动端 H5）与管理端（PC 后台）双前端。

## 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 后端框架 | Spring Boot | 4.1.0（JDK 25.0.3） |
| ORM | MyBatis（mybatis-spring-boot-starter） | 3.5.19 / starter 4.1.0 |
| 分页 | PageHelper | 6.x |
| 数据库 | MySQL（WSL 直装） | 8.0.46 |
| 缓存 | Redis（Windows 本地版） | 8.8.0 |
| 认证 | Spring Security 7.1.0 + JJWT 0.13.0（双 Token） | - |
| AI | Spring AI 2.0.0 + DeepSeek（OpenAI 兼容接口，SSE 流式） | - |
| 会员端 | Vue 3.5.40 + Vite 8.2.0 + Vant 4.10 + Pinia 4.0.2 | Node.js 25.3.0 |
| 管理端 | Vue 3.5.40 + Vite 8.2.0 + Element Plus 2.14.4 + Pinia 4.0.2 | Node.js 25.3.0 |

## 目录结构

```
my/
├── backend/                  # Spring Boot 后端
│   ├── sql/                  # 数据库初始化脚本（17 张表 + 种子数据）
│   └── src/main/java/com/eldercare/
│       ├── core/             # 公共层：common/config/entity/mapper/filter/exception/util/enums/dto/vo
│       ├── api/              # 会员端接口（/api/member/**、/api/auth/**）
│       └── admin/            # 管理端接口（/api/admin/**）
├── member/                   # 会员端前端（Vue3 + Vant，端口 5173）
├── admin/                    # 管理端前端（Vue3 + Element Plus，端口 5174）
├── docker-compose.yml        # 基础设施容器（仅容器化部署用）
└── docs/                     # 部署文档、API 文档
```

## 本地运行

需要 JDK、Maven、Node.js、MySQL 和 Redis。版本依赖以 pom.xml 和各前端 package.json 为准。

在 backend 目录创建本地配置：

```powershell
Copy-Item application-local.properties.example application-local.properties
# 编辑本地文件，填写数据库密码、API Key 和随机 JWT 密钥。
mvn spring-boot:run
```

application-local.properties 已加入 Git 忽略规则。启动时应以 backend 为工作目录；部署环境也可以直接设置 DB_PASSWORD、DEEPSEEK_API_KEY、JWT_SECRET、ADMIN_RESET_PASSWORD 等环境变量。环境变量优先于本地文件。

.env.example 用于说明变量；Spring Boot 不会自动读取 .env 文件。Docker Compose 可以通过 --env-file 指定自行填写的 .env 文件。

会员端和管理端分别在 member、admin 目录执行：

```powershell
npm ci
npm run dev
```

数据库需提前准备。本目录当前缺少旧文档提到的 sql 初始化脚本，首次从空数据库启动前需补齐脱敏的建表和演示数据脚本；不要提交实际业务数据库备份。

## 公开仓库说明

- 不提供本机账号或密码，请在自己的环境创建账户。
- 真实密钥只放在环境变量或被忽略的本地配置中。
- 本地数据、上传文件、日志、依赖、模型和个人简历不进入版本库。
- 认证采用 JWT 双 Token 和 Redis 黑名单；AI 咨询支持 SSE 流式响应。

