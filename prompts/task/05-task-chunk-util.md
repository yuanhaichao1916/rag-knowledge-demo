遵循项目基线 prompt-base.md。
本轮任务：生成 util/ChunkUtil.java
要求：
1. 固定长度分块工具，入参：文本、maxChunkSize、overlap
2. 按字符切分，带重叠overlap，不做语义切分
3. 空文本、空白文本做校验，返回空集合
4. 带注释，简单单元测试注释写在类上
5. 只输出 ChunkUtil.java
