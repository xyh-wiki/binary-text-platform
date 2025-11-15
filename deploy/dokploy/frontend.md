# Binary Text Frontend · Dokploy 说明

- **Context（构建上下文）**：仓库根目录 `binary-text-platform/`
- **Dockerfile**：`deploy/docker/Dockerfile.frontend`
- **内部端口**：`80`
- **建议域名**：`binaryextract.xyh.wiki`

构建镜像时需设置构建参数 `VITE_API_BASE_URL`，例如：

```bash
VITE_API_BASE_URL=https://binaryextract-api.xyh.wiki
```
