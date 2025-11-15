# Binary Text Backend · Dokploy 说明

- **Context（构建上下文）**：仓库根目录 `binary-text-platform/`
- **Dockerfile**：`deploy/docker/Dockerfile.backend`
- **内部端口**：`8080`
- **建议域名**：`binaryextract-api.xyh.wiki`

构建时可以通过 Dokploy 环境变量注入 `JAVA_OPTS`，例如：

```bash
JAVA_OPTS=-Xms512m -Xmx1024m
```
