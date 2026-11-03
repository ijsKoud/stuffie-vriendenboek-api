package nl.klrnbk.daan.stuffie.api.privacy.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nl.klrnbk.common.responses.error.BaseErrorResponses
import nl.klrnbk.daan.stuffie.api.privacy.api.facade.ScheduledTasksFacade
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@BaseErrorResponses
@RestController
@RequestMapping("/v1/scheduled")
@SecurityRequirement(name = "scheduled-api-key")
class ScheduledTasksController(
    private val scheduledTasksFacade: ScheduledTasksFacade,
) {
    @PostMapping("/erasure")
    @Operation(
        summary = "Replays the tombstone log to ensure full requested data deletion",
        description = "Meant for a scheduler. Sends every erasure to every platform again; needs the x-api-key header.",
    )
    @ApiResponse(
        responseCode = "204",
        description = "Job succeeded",
        content = [Content(mediaType = "*/*", schema = Schema(implementation = Void::class))],
    )
    fun runErasureReplay(
        @RequestHeader("x-api-key", required = false) apiKey: String?,
    ) = scheduledTasksFacade.runErasureReplay(apiKey)
}
