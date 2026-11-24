遵循项目基线 prompt-base.md。
本轮任务：Agent工具调用
1. tool/RagTools.java 使用@Tool注解
2. AgentController
工具实现：
1. 获取当前服务时间
2. 根据doc_id查询该文档chunk总数
要求：
1. LLM自主判断调用工具，不是强制调用
2. 打印工具调用日志
3. swagger接口
4. 输出文件标注文件名
