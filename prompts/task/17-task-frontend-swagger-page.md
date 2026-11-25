遵循项目基线 prompt-base.md 中的「前端约定」。

本轮任务：接口文档页（frontend/src/views/SwaggerUI.vue）

实现方式（三选一，按复杂度递增，本轮只做最简单的）：
1. **推荐**：iframe 内嵌后端 Swagger UI
   - src="http://localhost:8080/swagger-ui.html"
   - 开发环境前后端同机时可用，生产部署后需改 URL 为实际后端地址
   - 加输入框允许用户覆盖 Swagger UI 地址（适配不同环境）
2. 备选：自己构建 OpenAPI 渲染组件（工作量大，**不做**）
3. 备选：后端开启 springdoc.swagger-ui.url 参数（让前端能跨域加载），同 1

页面：
- Element Plus PageContainer 布局，标题「接口文档」
- iframe 宽度 100%、高度 calc(100vh - 180px)、无边框
- 顶部一行 el-input + 刷新按钮，默认值 http://localhost:8080/swagger-ui.html
