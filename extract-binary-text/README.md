# 二进制文件纯文本提取平台

## 📖 项目简介

本项目是一个基于 **Spring Boot + Vue2 + Bootstrap4** 的轻量级文书提取平台，
用于对 PDF、OFD、Word、Excel、HTML、TXT 等文件进行类型识别与纯文本提取。

系统采用后端统一接口调用内部方法 `GetTypeAndContent.getFileTypeAndContent(byte[])` 完成内容提取，
并提供：

- Web 页面文件上传与在线预览
- 提取历史记录保存与查询
- Swagger 在线接口调试
- 离线运行（本地前端依赖）

---

## 🧩 功能特性

| 功能模块 | 描述 |
|-----------|------|
| 文件上传与提取 | 支持上传 PDF/OFD/Word/Excel/TXT 等格式，识别文件类型并提取纯文本 |
| 文件类型自动判断 | 使用项目内 `GetTypeAndContent` 工具类自动识别文件类型 |
| 文本提取 | 自动抽取文本内容，支持中英文、RTF、HTML、OFD 等格式 |
| 历史记录 | 保存每次提取结果，可按时间、文件名、状态等查询 |
| 统计分析 | 提供今日提取次数、失败次数、历史总量 |
| 前端 UI | 支持文件上传、实时预览、展开/收起长文本 |
| Swagger | 提供 `/swagger-ui.html` 接口调试页面 |
| 离线运行 | 所有 JS/CSS 均可本地部署，无需访问互联网 |

---

## ⚙️ 环境要求

- **JDK 1.8+**
- **Maven 3.6+**
- **IDEA / Eclipse**
- （可选）Postman / 浏览器测试工具

---

## 📦 项目结构

```
extract-binary-text/
├── src/
│   ├── main/
│   │   ├── java/wiki/xyh/
│   │   │   ├── utils/GetTypeAndContent.java
│   │   │   ├── web/ExtractApplication.java
│   │   │   ├── web/controller/ExtractController.java
│   │   │   ├── web/service/ExtractHistoryService.java
│   │   │   └── web/bean & dto/
│   │   └── resources/
│   │       ├── static/index.html
│   │       ├── static/libs/
│   │       │   ├── bootstrap.min.css
│   │       │   ├── vue.min.js
│   │       │   ├── axios.min.js
│   │       │   ├── remixicon.css
│   │       │   └── fonts/remixicon.woff2 等
│   │       └── application.yml
└── pom.xml
```

---

## 🚀 启动方式

1. **导入项目**
   ```bash
   mvn clean install
   ```

2. **在 IDEA 中运行**
   - 运行类 `wiki.xyh.web.ExtractApplication`
   - 控制台出现 “Tomcat started on port(s): 8080” 即启动成功

3. **访问页面**
   ```
   http://localhost:8080/index.html
   ```

4. **访问 Swagger**
   ```
   http://localhost:8080/swagger-ui.html
   ```

---

## 🧪 接口说明

### 1. 上传文件提取
`POST /api/extract`

| 参数 | 类型 | 说明 |
|------|------|------|
| file | File | 上传文件（PDF/OFD/Word/TXT 等） |
| mode | String | TYPE_AND_CONTENT / TYPE_ONLY / CONTENT_ONLY |
| remark | String | 可选备注说明 |

**返回示例：**
```json
{
  "success": true,
  "fileName": "示例文书.pdf",
  "fileType": "PDF",
  "content": "……纯文本内容……",
  "errorMessage": null,
  "remark": "测试文件",
  "historyId": 1
}
```

### 2. 查询历史记录
`GET /api/history`

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码（从 1 开始） |
| pageSize | int | 每页数量 |
| fileNameLike | String | 文件名模糊查询 |
| fileType | String | 文件类型过滤 |
| success | Boolean | 成功/失败过滤 |
| startDate, endDate | Date | 日期范围过滤 |

### 3. 查看详情
`GET /api/history/{id}`

### 4. 统计数据
`GET /api/history/stats`

---

## 🧱 离线前端部署

如服务器无外网，请将以下文件放入本地 `static/libs/`：

| 文件 | 来源 |
|------|------|
| bootstrap.min.css | https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css |
| vue.min.js | https://cdn.jsdelivr.net/npm/vue@2.7.16/dist/vue.js |
| axios.min.js | https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js |
| remixicon.css | https://cdn.jsdelivr.net/npm/remixicon@3.5.0/fonts/remixicon.css |
| remixicon.woff2 / .woff | https://cdn.jsdelivr.net/npm/remixicon@3.5.0/fonts/ |

并修改 index.html 的引用：
```html
<link rel="stylesheet" href="libs/bootstrap.min.css">
<script src="libs/vue.min.js"></script>
<script src="libs/axios.min.js"></script>
<link rel="stylesheet" href="libs/remixicon.css">
```

---

## ⚠️ 注意事项

- 若 `type=UNKNOWN`，系统自动标记提取失败；
- 预览区文本较长时，可展开/收起查看完整内容；
- 历史记录为内存存储（程序重启后清空）；
- 若需持久化历史记录，可扩展为 MySQL / SQLite 存储。

---

## 📄 License

版权所有 © XYH，保留所有权利。
