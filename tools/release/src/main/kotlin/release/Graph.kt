package release

data class Project(val path: String, val dir: String, val deps: Set<String>)

/** Files that affect every project (build conventions, wrapper, Dockerfile). */
private val GLOBAL_PATHS = listOf("build-logic/", "gradle/", "gradlew", "gradlew.bat", "build.gradle.kts", "settings.gradle.kts", "gradle.properties", "Dockerfile")

class Graph(projects: Collection<Project>) {
    val projects: Map<String, Project> = projects.associateBy { it.path }

    /** Deployable services: everything under apps/. */
    val services: List<Project> get() = projects.values.filter { it.path.startsWith(":apps:") }.sortedBy { it.path }

    fun transitiveDeps(path: String): Set<String> {
        val seen = mutableSetOf<String>()
        val queue = ArrayDeque(projects[path]?.deps.orEmpty())
        while (queue.isNotEmpty()) {
            val next = queue.removeFirst()
            if (seen.add(next)) queue.addAll(projects[next]?.deps.orEmpty())
        }
        return seen
    }

    fun isGlobal(file: String) = GLOBAL_PATHS.any { file == it || file.startsWith(it) }

    /** The project owning [file]: the one with the longest matching directory. */
    fun owner(file: String): Project? =
        projects.values.filter { file.startsWith(it.dir + "/") }.maxByOrNull { it.dir.length }

    /** Projects that must be rebuilt for [files]: the owners plus everything depending on them (all, for global files). */
    fun affected(files: Collection<String>): Set<String> {
        if (files.any(::isGlobal)) return projects.keys
        val changed = files.mapNotNull { owner(it)?.path }.toSet()
        return projects.keys.filter { it in changed || transitiveDeps(it).any(changed::contains) }.toSet()
    }

    companion object {
        /** Parses the `path<TAB>dir<TAB>dep,dep` lines written by the `projectGraph` Gradle task. */
        fun parse(tsv: String) = Graph(
            tsv.lines().filter { it.isNotBlank() }.map { line ->
                val (path, dir, deps) = line.split('\t').let { Triple(it[0], it[1], it.getOrElse(2) { "" }) }
                Project(path, dir, deps.split(',').filter { it.isNotBlank() }.toSet())
            },
        )
    }
}
