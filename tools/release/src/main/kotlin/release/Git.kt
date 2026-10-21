package release

import java.io.File

private fun git(vararg args: String): String {
    val p = ProcessBuilder("git", *args).redirectErrorStream(false).start()
    val out = p.inputStream.bufferedReader().readText()
    check(p.waitFor() == 0) { "git ${args.joinToString(" ")} failed: ${p.errorStream.bufferedReader().readText()}" }
    return out
}

fun lastVersion(service: String): Pair<String, Version>? =
    git("tag", "--list", "$service/v*").lines().filter { it.isNotBlank() }
        .mapNotNull { t -> runCatching { t to Version.parse(t.removePrefix("$service/v")) }.getOrNull() }
        .maxByOrNull { it.second }

/** Commits in [range] (a git revision range) with the files each touched. Merge commits are skipped. */
fun commitsIn(range: String): List<Commit> =
    git("log", "--no-merges", "--no-renames", "--name-only", "--format=%x1e%s%x1f%b%x1f", range)
        .split('\u001e').filter { it.isNotBlank() }.map { chunk ->
            val (subject, body, files) = chunk.split('\u001f')
            Commit(subject, body, files.lines().filter { it.isNotBlank() })
        }

fun changedFiles(base: String): List<String> =
    git("diff", "--name-only", "--no-renames", "$base...HEAD").lines().filter { it.isNotBlank() }

fun readGraph(path: String) = Graph.parse(File(path).readText())
