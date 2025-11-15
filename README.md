# Binary Text Extraction Platform (binaryextract.xyh.wiki)

本项目基于现有 `extract-binary-text` 模块，对其进行 Web 化封装与前后端分离部署：
- **核心提取逻辑** 完全复用 `GetTypeAndContent`，不修改原有实现；
- **后端 binary-text-api** 仅负责接收文件并调用提取方法，精简轻量，减少服务端压力；
- **前端 binary-text-frontend** 负责文件队列、并发控制、结果展示、复制与下载等功能，将更多逻辑前移到浏览器端；
- 默认页面语言为 **英文**，同时支持 **中英文切换**；
- 前端与后端分别通过 Dokploy + GitHub 集成，以两个域名独立部署：
  - 前端域名：`https://binaryextract.xyh.wiki`
  - 后端域名：`https://binaryextract-api.xyh.wiki`

## 1. 模块结构

- `extract-binary-text/`  
  原始纯文本提取工具模块（保持你的实现不变，包含 GetTypeAndContent 等方法）

- `binary-text-api/`  
  Spring Boot 精简后端服务，仅暴露：
  - `POST /api/extract/single`
  - `POST /api/extract/batch`

- `binary-text-frontend/`  
  Vite + React 前端页面：
  - 默认英文 UI
  - 顶部右侧 **English / 中文** 语言切换
  - 文件队列 + 前端并发控制
  - 文本预览、复制、下载 `.txt`

## 2. 后端本地运行（binary-text-api）

```bash
cd binary-text-platform-v2
mvn -pl binary-text-api -am clean package
java -jar binary-text-api/target/binary-text-api-1.0.0.jar
```

启动后默认监听端口为 `8080`。

提供的接口示例：

- `POST /api/extract/single`  
  - Content-Type: `multipart/form-data`
  - 表单字段：`file`（单个文件）

- `POST /api/extract/batch`  
  - Content-Type: `multipart/form-data`
  - 表单字段：`files`（多个文件）

并内置 Swagger（springdoc-openapi）支持，默认可访问：

- `http://localhost:8080/swagger-ui.html`

> 注意：`binary-text-api` 模块依赖 `extract-binary-text`，请保证该模块 `pom.xml` 中 `groupId/artifactId/version` 与本工程一致（默认 `wiki.xyh:extract-binary-text:1.0-SNAPSHOT`）。

## 3. 前端本地运行（binary-text-frontend）

```bash
cd binary-text-platform-v2/binary-text-frontend
npm install
# 开发模式（默认调用本地后端）
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

浏览器访问 `http://localhost:5173` 即可。

前端特性：

- 默认英文 UI；
- 右上角语言切换按钮：`English` / `中文`；
- 前端维护文件任务队列，控制并发数（当前默认 2，可在 `App.tsx` 中修改）；
- 每个任务显示：文件名、大小、状态、类型、错误信息、内容片段；
- 支持复制文本到剪贴板与下载 `.txt` 文件。

## 4. Dokploy + GitHub 部署建议

### 4.1 后端：binary-text-api → binaryextract-api.xyh.wiki

1. 将本仓库推送到 GitHub（例如 `xyh-wiki/binary-text-platform`）。
2. 在 Dokploy 中创建 **后端应用**：
   - 选择来源：GitHub 仓库；
   - 子目录（Context）：`binary-text-api`；
   - 构建方式：Dockerfile（使用该目录下的 `Dockerfile`）；
   - 暴露端口：容器内部 `8080`；
   - 域名绑定：`binaryextract-api.xyh.wiki`；
   - 其他环境变量可按需配置（如 JVM 参数、日志级别等）。

3. Dokploy 会自动：
   - 从 GitHub 拉取代码；
   - 执行 `mvn ... package`（如你在 Dokploy 中配置）或直接用 Dockerfile 构建；
   - 部署容器并通过 Traefik 暴露 `https://binaryextract-api.xyh.wiki`。

### 4.2 前端：binary-text-frontend → binaryextract.xyh.wiki

1. 在 Dokploy 中创建 **前端应用**：
   - 选择来源：同一个 GitHub 仓库；
   - 子目录（Context）：`binary-text-frontend`；
   - 构建方式：Dockerfile（该目录自带）；
   - 域名绑定：`binaryextract.xyh.wiki`。

2. 在 Dokploy 环境变量中配置：

   - `VITE_API_BASE_URL=https://binaryextract-api.xyh.wiki`

   这样前端在构建阶段就会把后端域名写入静态资源中，部署完成后即可通过公网调用。

> 如果你使用的是 Dokploy 的“自动构建 + 运行”，它会在每次 GitHub push 后自动重新构建镜像并滚动更新容器，非常适合该前后端分离结构。

## 5. 二次开发与习惯说明

- 所有新增 Java 类都已按你的要求添加：
  - 类头注释：`Author:XYH Date:yyyy-MM-dd Description:`
  - 方法上附带详细中文注释。
- 前端代码使用 TypeScript，并在关键逻辑处提供了中文注释，便于后续维护与扩展。
- SEO 相关：
  - `binary-text-frontend/index.html` 中已设置基础 `<title>`、`description`、`keywords`；
  - 可根据后续推广需求再补充 OG/meta 标签或 Google Ads 代码。

你可以直接解压本 Zip 包导入 IDE（后端用 IDEA，前端用 VS Code/ WebStorm 等）进行二次修改。

