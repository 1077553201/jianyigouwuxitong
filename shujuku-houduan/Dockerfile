FROM eclipse-temurin:25-jre-noble

WORKDIR /app

# 直接复制本地已经存在的 app.jar 
COPY ./app.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]