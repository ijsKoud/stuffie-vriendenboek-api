package nl.klrnbk.stuffie.api.service

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/** Runs at every startup, so restoring an old backup and restarting the service re-erases deleted accounts. */
@Component
class ErasureReplayRunner(
    private val erasureService: ErasureService,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(ErasureReplayRunner::class.java)

    override fun run(args: ApplicationArguments) {
        val erased = erasureService.replay()
        if (erased > 0) logger.warn("Replayed erasures: $erased account(s) found in the data and erased again")
    }
}
