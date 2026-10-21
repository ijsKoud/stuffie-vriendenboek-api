pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kotlin-monorepo-template"

// Every directory with a build.gradle.kts under apps/ and packages/ is a project; no edits needed to add one.
listOf("apps", "packages").forEach { group ->
    rootDir.resolve(group).listFiles()
        ?.filter { it.resolve("build.gradle.kts").exists() }
        ?.sortedBy { it.name }
        ?.forEach { include(":$group:${it.name}") }
}
include(":tools:release")
