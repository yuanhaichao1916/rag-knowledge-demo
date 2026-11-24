# 运行时镜像：JDK 21
FROM eclipse-temurin:21-jdk

# 工作目录
WORKDIR /app

# 复制打包好的 jar（需先在本地执行 mvn clean package）
COPY target/rag-knowledge-demo-1.0.0.jar app.jar

# 时区
ENV TZ=Asia/Shanghai
# JVM 参数，可通过 docker run -e JAVA_OPTS=... 覆盖
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 服务端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
