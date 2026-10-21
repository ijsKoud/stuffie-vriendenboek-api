plugins {
    id("buildlogic.kotlin-library")
    alias(libs.plugins.kotlin.spring)
    `java-library`
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}"))
    api("org.springframework:spring-web")
    api("io.swagger.core.v3:swagger-annotations:2.2.31")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
