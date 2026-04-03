# 빌드 스테이지
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 런타임 실행 스테이지
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 컨테이너 내부에서 앱이 사용하는 포트
EXPOSE 8080

# build 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 타임존, 헬스체크용 curl
ENV TZ=Asia/Seoul
RUN apt-get update \
 && apt-get install -y curl tzdata \
 && rm -rf /var/lib/apt/lists/*

#안정성을위해 추가, compse 환경변수가 우선순위 1위지만 없으면 이걸 사용, 항상 값이 있다고 넣어 안정성을 높임
ENV JVM_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JVM_OPTS -jar app.jar"]