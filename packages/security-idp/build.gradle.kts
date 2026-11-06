plugins {
    id("buildlogic.kotlin-library")
    alias(libs.plugins.kotlin.spring)
    `java-library`
}

dependencies {
    api(project(":packages:common"))
    api(project(":packages:spring"))
    api("org.springframework.boot:spring-boot-starter-security")
    implementation("com.auth0:java-jwt:4.6.2")
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
