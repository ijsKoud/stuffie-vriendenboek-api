package release

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReleaseTest {
    // hello and other both use common; other also uses extra.
    private val graph = Graph.parse(
        """
        :apps:hello	apps/hello	:packages:common
        :apps:other	apps/other	:packages:common,:packages:extra
        :apps:solo	apps/solo	
        :packages:common	packages/common	
        :packages:extra	packages/extra	:packages:common
        :tools:release	tools/release	
        """.trimIndent(),
    )

    private fun c(subject: String, vararg files: String, body: String = "") = Commit(subject, body, files.toList())
    private fun bump(service: String, vararg commits: Commit) = bumpFor(graph, graph.projects.getValue(":apps:$service"), commits.toList())

    @Test
    fun conventionalTitles() {
        assertTrue(isConventional("feat(hello): add endpoint"))
        assertTrue(isConventional("fix!: drop field"))
        assertTrue(!isConventional("added stuff"))
        assertTrue(!isConventional("feat: "))
    }

    @Test
    fun ownCommitsMapTypeToBump() {
        assertEquals(Bump.MINOR, bump("hello", c("feat: x", "apps/hello/A.kt")))
        assertEquals(Bump.PATCH, bump("hello", c("fix: x", "apps/hello/A.kt")))
        assertEquals(Bump.PATCH, bump("hello", c("chore: x", "apps/hello/A.kt")))
        assertEquals(Bump.MAJOR, bump("hello", c("feat!: x", "apps/hello/A.kt")))
        assertEquals(Bump.MAJOR, bump("hello", c("fix: x", "apps/hello/A.kt", body = "BREAKING CHANGE: gone")))
    }

    @Test
    fun highestBumpWins() {
        assertEquals(Bump.MINOR, bump("hello", c("fix: a", "apps/hello/A.kt"), c("feat: b", "apps/hello/B.kt")))
    }

    @Test
    fun packageChangesAreAtMostMinorForDependents() {
        assertEquals(Bump.PATCH, bump("hello", c("feat: x", "packages/common/A.kt")))
        assertEquals(Bump.MINOR, bump("hello", c("feat!: x", "packages/common/A.kt")))
        assertEquals(Bump.PATCH, bump("other", c("fix: x", "packages/extra/A.kt")))
        // transitive: other -> extra -> common
        assertEquals(Bump.PATCH, bump("other", c("fix: x", "packages/common/A.kt")))
    }

    @Test
    fun unrelatedAndNonServiceChangesBumpNothing() {
        assertNull(bump("solo", c("feat: x", "packages/common/A.kt")))
        assertNull(bump("hello", c("docs: x", "README.md", "tools/release/A.kt", ".github/workflows/a.yaml")))
    }

    @Test
    fun globalFilesBumpEveryService() {
        assertEquals(Bump.PATCH, bump("solo", c("feat: x", "gradle/libs.versions.toml")))
    }

    @Test
    fun ownerPrefersTheLongestDirectory() {
        assertEquals(":apps:hello", graph.owner("apps/hello/src/A.kt")?.path)
        assertNull(graph.owner("apps/hello-other/A.kt"))
    }

    @Test
    fun affectedIncludesDependents() {
        assertEquals(setOf(":packages:common", ":packages:extra", ":apps:hello", ":apps:other"), graph.affected(listOf("packages/common/A.kt")))
        assertEquals(graph.projects.keys, graph.affected(listOf("Dockerfile")))
    }

    @Test
    fun planComputesVersions() {
        val releases = plan(
            graph,
            previous = { if (it.path == ":apps:hello") Version(1, 2, 3) else null },
            commitsSince = { listOf(c("feat: x", "apps/hello/A.kt", "apps/solo/B.kt")) },
        )
        assertEquals(listOf("hello/v1.3.0", "solo/v0.1.0"), releases.map { it.tag })
    }

    @Test
    fun majorResetsMinorAndPatch() = assertEquals("2.0.0", Version(1, 4, 7).bump(Bump.MAJOR).toString())
}
