遵循项目基线 prompt-base.md。

本轮任务：更新基线并补齐后端跨域配置（前端能调后端的前置条件）

一、更新 prompts/prompt-base.md
在「功能约束」之后、「工程规范」之前，新增一节：

## 前端约定
1. 技术栈：Vue 3.4 + Vite 5 + Element Plus 2.7
2. 目录：仓库根目录 `frontend/`，与后端 `src/` 平级
3. 运行方式：开发模式 Vite 独立 dev server（5173 端口），通过 proxy 转发 `/api` 和 `/v3` 到后端 8080
4. 构建产物：`frontend/dist/`，可选部署到 Nginx 或 Spring Boot `static/`
5. 页面范围：问答主界面、文档管理、Agent 工具调试台、接口文档（Swagger UI 内嵌）

二、修改 application.yml，新增前端跨域配置占位（不直接改 Java 配置类）
在文件末尾新增：

# ---------------- 前端跨域（开发环境） ----------------
# 生产环境建议用 Nginx 同域反向代理替代 CORS，此处仅用于本地联调
cors:
  allowed-origins:
    - http://localhost:5173
    - http://127.0.0.1:5173
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true

三、新增 CORS 配置类（com.demo.rag.config.WebMvcConfig.java）
- 实现 WebMvcConfigurer，重写 addCorsMappings
- 读取上面 yml 中的 cors 配置
- 全局允许跨域（开发环境够用，后续按需收敛）

输出：只改 3 个文件，不要额外文件。
