package nl.klrnbk.stuffie.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import nl.klrnbk.common.responses.error.BadRequestResponse
import nl.klrnbk.common.responses.error.BaseErrorResponses
import nl.klrnbk.common.responses.error.NotFoundResponse
import nl.klrnbk.security.idp.models.JwtAuthenticationToken
import nl.klrnbk.stuffie.api.facade.StuffieFacade
import nl.klrnbk.stuffie.api.models.RainbowBridgeRequest
import nl.klrnbk.stuffie.api.models.SharingRequest
import nl.klrnbk.stuffie.api.models.StuffieDetails
import nl.klrnbk.stuffie.api.models.StuffieRequest
import nl.klrnbk.stuffie.datasource.models.StuffieStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SecurityRequirement(name = "api-key")
@RequestMapping("/v1/stuffies")
@BaseErrorResponses
class StuffieController(
    private val stuffieFacade: StuffieFacade,
) {
    @GetMapping
    @Operation(summary = "List your stuffies, newest first", description = "Optionally only the ones that are home or on the Rainbow Bridge.")
    fun list(
        authentication: JwtAuthenticationToken,
        @RequestParam("status", required = false) status: StuffieStatus?,
    ) = stuffieFacade.list(authentication.principal, status)

    @PostMapping
    @Operation(summary = "Add a stuffie")
    @BadRequestResponse
    fun create(
        authentication: JwtAuthenticationToken,
        @Valid @RequestBody request: StuffieRequest,
    ): ResponseEntity<StuffieDetails> = stuffieFacade.create(authentication.principal, request)

    @GetMapping("/{id}")
    @Operation(summary = "Get one of your stuffies")
    @NotFoundResponse
    fun get(
        authentication: JwtAuthenticationToken,
        @PathVariable id: String,
    ) = stuffieFacade.get(authentication.principal, id)

    @PutMapping("/{id}")
    @Operation(summary = "Replace the details of a stuffie", description = "Blank text clears a field. Omit photoId to keep the photo.")
    @NotFoundResponse
    @BadRequestResponse
    fun update(
        authentication: JwtAuthenticationToken,
        @PathVariable id: String,
        @Valid @RequestBody request: StuffieRequest,
    ) = stuffieFacade.update(authentication.principal, id, request)

    @PostMapping("/{id}/rainbow-bridge")
    @Operation(
        summary = "Say goodbye: move a stuffie to the Rainbow Bridge",
        description = "There is no permanent delete. A stuffie that is already on the bridge answers 409.",
    )
    @NotFoundResponse
    @BadRequestResponse
    fun sendToRainbowBridge(
        authentication: JwtAuthenticationToken,
        @PathVariable id: String,
        @Valid @RequestBody request: RainbowBridgeRequest,
    ) = stuffieFacade.sendToRainbowBridge(authentication.principal, id, request)

    @PostMapping("/{id}/restore")
    @Operation(summary = "Bring a stuffie back from the Rainbow Bridge")
    @NotFoundResponse
    fun restore(
        authentication: JwtAuthenticationToken,
        @PathVariable id: String,
    ) = stuffieFacade.restore(authentication.principal, id)

    @PutMapping("/{id}/sharing")
    @Operation(
        summary = "Turn the public read-only link on or off",
        description = "Turning it on creates a token (the same one when called again); turning it off revokes the link.",
    )
    @NotFoundResponse
    fun setSharing(
        authentication: JwtAuthenticationToken,
        @PathVariable id: String,
        @RequestBody request: SharingRequest,
    ) = stuffieFacade.setSharing(authentication.principal, id, request.enabled)
}
