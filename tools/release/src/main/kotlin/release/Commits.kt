package release

data class Commit(val subject: String, val body: String, val files: List<String>)

enum class Bump { PATCH, MINOR, MAJOR }

private val HEADER = Regex("""^(\w+)(\([^)]*\))?(!)?: \S.*""")

fun isConventional(title: String) = HEADER.matches(title.trim())

/** A conventional-commit breaking change: `type!:` or a `BREAKING CHANGE:` footer. */
fun isBreaking(c: Commit): Boolean =
    HEADER.matchEntire(c.subject.trim())?.groupValues?.get(3) == "!" ||
        c.body.lines().any { it.startsWith("BREAKING CHANGE:") || it.startsWith("BREAKING-CHANGE:") }

fun commitType(c: Commit): String = HEADER.matchEntire(c.subject.trim())?.groupValues?.get(1)?.lowercase() ?: "other"
