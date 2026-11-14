plugins {
    id("buildlogic.spring-service")
    alias(libs.plugins.kotlin.jpa)
}

dependencies {
    implementation(project(":packages:common"))
    implementation(project(":packages:spring"))
    implementation(project(":packages:security-idp"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("software.amazon.awssdk:s3:2.59.1")
    implementation("net.coobird:thumbnailator:0.4.21")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("com.auth0:java-jwt:4.6.2")
    testRuntimeOnly("com.h2database:h2")
}

// Local development runs against the `local` profile (Postgres from local/docker-compose.yml, photos on disk).
tasks.bootRun { systemProperty("spring.profiles.active", "local") }
