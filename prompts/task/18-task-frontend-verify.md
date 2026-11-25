遵循项目基线 prompt-base.md + changelog.md。

本轮任务：前端联调验证 + 提交推送

验证清单：
1. npm install && npm run build 成功（无编译错误、无 Element Plus 样式丢失）
2. 本地同时启动：后端（8080，需要数据库和 API Key）+ 前端 dev（5173）
3. 四个页面分别验证：
   - 文档管理：上传 txt/md → 看到 docId → 查看分片 → 删除
   - RAG 问答：输入问题 → 普通问答 → SSE 流式 → 引用片段显示
   - Agent 调试台：预置问题触发工具调用 → 返回时间或分片数
   - Swagger UI：iframe 正确加载
4. 跨域验证：关闭 vite proxy，直接请求后端时浏览器 Network 面板确认 cors 头正确
5. 提交 git：单独 commit「feat: 新增 Vue3 前端」，不要和后端改动混在一起
6. 推送到 GitHub

记录：所有失败点写入 prompts/changelog.md，标注【19】。
