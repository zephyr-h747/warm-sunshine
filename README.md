# Warm Sunshine · 智能养老社区管理系统

面向养老社区的服务平台，包含会员端 H5 和管理后台，覆盖健康数据记录、健康评测、体检预约、社区活动与积分管理。

## 技术与功能

- 后端：Java、Spring Boot、MyBatis、MySQL、Redis、Spring Security、JWT。
- 前端：Vue 3；会员端使用 Vant，管理端使用 Element Plus。
- AI：通过 Spring AI 接入 DeepSeek，支持 SSE 流式健康咨询、多轮对话和评测失败兜底。
- 预约及积分：数据库条件更新、事务控制和积分 FIFO 扣减。

## 项目目录

- `backend/`：后端服务与配置示例。
- `member/`：会员端。
- `admin/`：管理后台。

## 运行

后端配置与启动步骤见 [backend/README.md](backend/README.md)。本地配置使用 `backend/application-local.properties.example` 创建，真实凭据不得提交。

会员端和管理端分别进入对应目录执行 `npm ci` 和 `npm run dev`。

## 当前限制


