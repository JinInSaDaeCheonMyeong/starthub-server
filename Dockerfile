FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y \
    libreoffice \
    libreoffice-writer \
    fonts-nanum \
    fonts-nanum-extra \
    fonts-noto-cjk \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /tmp/document-conversion

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
