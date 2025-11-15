# 架构概览

- `extract-binary-text-core`：封装 `GetTypeAndContent` 等纯文本抽取方法，负责识别文件类型并抽取纯文本。
- `binary-text-backend`：Spring Boot API，仅做 HTTP 请求封装、参数校验和结果返回。
- `binary-text-frontend`：前端队列与并发控制尽量放在浏览器侧，后端只负责调用抽取方法。
