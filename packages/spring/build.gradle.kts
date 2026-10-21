plugins {
    id("buildlogic.kotlin-library")
    alias(libs.plugins.kotlin.spring)
    `java-library`
}

dependencies {
    api(project(":packages:common"))
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.springframework.boot:spring-boot-starter-webmvc")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
