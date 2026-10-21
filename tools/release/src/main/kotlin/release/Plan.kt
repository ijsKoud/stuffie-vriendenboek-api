package release

data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
    override fun compareTo(other: Version) = compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch })
    override fun toString() = "$major.$minor.$patch"

    fun bump(b: Bump) = when (b) {
        Bump.MAJOR -> Version(major + 1, 0, 0)
        Bump.MINOR -> Version(major, minor + 1, 0)
        Bump.PATCH -> Version(major, minor, patch + 1)
    }

    companion object {
        fun parse(s: String): Version = s.split('.').map { it.toInt() }.let { Version(it[0], it[1], it[2]) }
    }
}

data class Release(val service: String, val project: String, val previous: Version?, val version: Version, val bump: Bump?) {
    val tag get() = "$service/v$version"
}

/** What a service's own commit means for it: feat is minor, breaking is major, everything else a patch. */
private fun ownBump(c: Commit) = when {
    isBreaking(c) -> Bump.MAJOR
    commitType(c) == "feat" -> Bump.MINOR
    else -> Bump.PATCH
}

/** What a dependency's (or global) commit means for a dependent: a break is a minor, everything else a patch. */
private fun dependencyBump(c: Commit) = if (isBreaking(c)) Bump.MINOR else Bump.PATCH

/** The bump [commits] imply for [service], or null when none of them touch it or its dependencies. */
fun bumpFor(graph: Graph, service: Project, commits: List<Commit>): Bump? {
    val deps = graph.transitiveDeps(service.path)
    return commits.mapNotNull { c ->
        val owners = c.files.mapNotNull { graph.owner(it)?.path }
        when {
            service.path in owners -> ownBump(c)
            c.files.any(graph::isGlobal) || owners.any(deps::contains) -> dependencyBump(c)
            else -> null
        }
    }.maxOrNull()
}

val FIRST_VERSION = Version(0, 1, 0)

/**
 * One release per affected service. [previous] and [commitsSince] are looked up per service
 * (its last tag and the commits after it), so a missed run is caught up by the next one.
 */
fun plan(graph: Graph, previous: (Project) -> Version?, commitsSince: (Project) -> List<Commit>): List<Release> =
    graph.services.mapNotNull { s ->
        val bump = bumpFor(graph, s, commitsSince(s)) ?: return@mapNotNull null
        val prev = previous(s)
        Release(s.dir.substringAfterLast('/'), s.path, prev, prev?.bump(bump) ?: FIRST_VERSION, bump)
    }
