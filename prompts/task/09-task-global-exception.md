遵循项目基线 prompt-base.md。
本轮任务：全局异常处理
1. config/GlobalExceptionConfig.java
2. 统一返回结果封装 Result.java
要求：
1. 全局异常捕获，统一code、msg、data
2. 参数校验异常、LLM调用异常、数据库异常分别处理
3. 不破坏SSE流式接口返回逻辑
4. 输出文件标注文件名
