遵循项目基线 prompt-base.md 中的「前端约定」。

本轮任务：文档管理页（frontend/src/views/DocManage.vue）

功能：
1. 上传区：Element Plus el-upload，仅支持 .txt / .md（accept），表单含 title + content 输入框
   - 上传后提示 docId 并追加到下方列表
   - 上传中显示 loading
2. 文档列表（可选功能，后端没直接 listAll 接口）：
   - 简化处理：上传历史用 localStorage 存 {docId, title, time}，不要伪造后端接口
   - 每行支持「查看分片」和「删除」
3. 分片查看弹窗：
   - 调用 GET /api/doc/{docId}/chunks
   - Element Plus el-table：title / content / createTime，不展示 embedding
4. 删除确认弹窗 + 调用 DELETE /api/doc/{docId}，成功后刷新本地列表

布局：上方上传区 + 下方列表区；表格分页 10 条/页。
