// Writes the project dependency graph (path<TAB>dir<TAB>comma-separated project deps) for tools/release.
tasks.register("projectGraph") {
    val out = layout.buildDirectory.file("project-graph.tsv")
    outputs.file(out)
    // shortcut: reads other projects at execution time, so this task is not configuration-cache compatible; fine until the cache is enabled.
    doLast {
        val lines = subprojects.filter { it.buildFile.exists() }.map { p ->
            val deps = p.configurations.flatMap { it.dependencies.withType<ProjectDependency>() }.map { it.path }.toSortedSet()
            "${p.path}\t${p.projectDir.relativeTo(rootDir).invariantSeparatorsPath}\t${deps.joinToString(",")}"
        }
        out.get().asFile.apply { parentFile.mkdirs() }.writeText(lines.sorted().joinToString("\n") + "\n")
    }
}
