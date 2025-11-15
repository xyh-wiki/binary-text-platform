# Binary Text Platform 后端 & 前端平台

本仓库用于管理「二进制文件纯文本抽取平台」的后端与前端代码，以及部署配置。

## 仓库结构

- `backend/`：Java 后端多模块项目（父 POM + 抽取核心库 + Spring Boot API）
- `frontend/`：Vite + React 前端项目，部署到 `binaryextract.xyh.wiki`
- `deploy/`：Dokploy / Docker / Traefik 相关配置
- `docs/`：说明文档（架构、接口、SEO 等）
