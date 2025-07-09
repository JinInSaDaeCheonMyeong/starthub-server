plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.3"
    kotlin("plugin.jpa") version "1.9.25"
    id("io.spring.dependency-management") version "1.1.7"
}

val springAiVersion by extra("1.0.0")

group = "com.JinInSaDaeCheonMyeong"
version = "0.0.1-SNAPSHOT"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // basic
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // websocket + stomp
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // ai
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // email
    implementation ("org.springframework.boot:spring-boot-starter-mail")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc")

    runtimeOnly("com.mysql:mysql-connector-j")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // testCode
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // jjwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // webflux
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // etc
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
