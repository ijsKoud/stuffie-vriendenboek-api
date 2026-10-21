package release

import kotlin.system.exitProcess

private const val USAGE = """usage:
  release check-title <title>
  release affected --graph FILE --base REF          Gradle task names to build/test, one per line
  release plan --graph FILE [--format json|markdown] [--pr-title T --pr-base REF]"""

fun main(args: Array<String>) {
    val opts = args.drop(1).chunked(2).filter { it.size == 2 }.associate { (k, v) -> k.removePrefix("--") to v }
    when (args.firstOrNull()) {
        "check-title" -> {
            val title = args.getOrNull(1) ?: fail(USAGE)
            if (!isConventional(title)) fail("PR title must follow Conventional Commits, e.g. 'feat(hello): add endpoint' or 'fix!: drop old field'. Got: $title")
        }
        "affected" -> {
            val graph = readGraph(opts["graph"] ?: fail(USAGE))
            graph.affected(changedFiles(opts["base"] ?: fail(USAGE)))
                .filter { graph.projects.getValue(it).dir.isNotEmpty() }
                .sorted().forEach { println("$it:build") }
        }
        "plan" -> {
            val graph = readGraph(opts["graph"] ?: fail(USAGE))
            val prTitle = opts["pr-title"]
            val prBase = opts["pr-base"]
            val pr = if (prTitle != null && prBase != null) Commit(prTitle, "", changedFiles(prBase)) else null
            val releases = plan(
                graph,
                previous = { lastVersion(it.dir.substringAfterLast('/'))?.second },
                commitsSince = { s ->
                    val tag = lastVersion(s.dir.substringAfterLast('/'))?.first
                    // In PR mode the real history ends at the base branch; the PR itself is appended as one squashed commit.
                    val head = prBase ?: "HEAD"
                    commitsIn(if (tag == null) head else "$tag..$head") + listOfNotNull(pr)
                },
            )
            println(if (opts["format"] == "markdown") markdown(releases, pr != null) else json(releases))
        }
        else -> fail(USAGE)
    }
}

private fun fail(msg: String): Nothing {
    System.err.println(msg)
    exitProcess(1)
}

private fun json(releases: List<Release>) = releases.joinToString(",", """{"releases":[""", "]}") {
    """{"service":"${it.service}","project":"${it.project}","previous":${it.previous?.let { v -> "\"$v\"" } ?: "null"},"version":"${it.version}","tag":"${it.tag}","bump":"${it.bump?.name?.lowercase()}"}"""
}

private fun markdown(releases: List<Release>, dryRun: Boolean): String {
    val head = if (dryRun) "### Release preview\nIf merged as titled, this PR would release:" else "### Release plan"
    if (releases.isEmpty()) return "$head\n\nNothing: no service is affected."
    return "$head\n\n| Service | Bump | Version |\n|---|---|---|\n" +
        releases.joinToString("\n") { "| `${it.service}` | ${it.bump?.name?.lowercase()} | ${it.previous ?: "new"} → **${it.version}** |" }
}
