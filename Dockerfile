FROM openjdk:17-jre

# JAR 파일을 /app 디렉터리에 복사
COPY app.jar /app/app.jar

# 작업 디렉터리 설정
WORKDIR /app

# 앱 실행
ENTRYPOINT ["java", "-jar", "app.jar"]