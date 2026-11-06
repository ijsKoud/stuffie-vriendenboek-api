package nl.klrnbk.daan.stuffie.api.privacy.api.service

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * Keeps the platforms honest: at every startup all tombstones are sent to all platforms again (a platform may have
 * restored an old backup), and periodically only the erasures that failed earlier are retried.
 */
@Component
class ErasureReplayRunnerService(
    private val tombstoneRegistryService: TombstoneRegistryService,
    private val platformDataDeletionService: PlatformDataDeletionService,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(ErasureReplayRunnerService::class.java)

    override fun run(args: ApplicationArguments) = replay(force = true)

    fun replay(force: Boolean) {
        val tombstones = tombstoneRegistryService.findAll()
        if (tombstones.isEmpty()) return

        val complete = platformDataDeletionService.erase(tombstones, force)
        logger.info("Replayed ${tombstones.size} erasure(s), force=$force, complete=$complete")
    }
}
