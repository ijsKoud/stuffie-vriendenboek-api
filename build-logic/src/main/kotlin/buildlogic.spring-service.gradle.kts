plugins {
    id("buildlogic.kotlin-library")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// CI passes -PreleaseVersion=<x.y.z> (derived from the service's git tag); local builds get a snapshot.
version = providers.gradleProperty("releaseVersion").getOrElse("0.0.0-SNAPSHOT")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Fixed name so the Dockerfile does not need to know the version.
tasks.bootJar { archiveFileName.set("app.jar") }
tasks.jar { enabled = false } // only the boot jar is shipped
