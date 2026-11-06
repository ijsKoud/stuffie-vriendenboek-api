package nl.klrnbk.stuffie.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nl.klrnbk.common.responses.error.BaseErrorResponses
import nl.klrnbk.security.idp.models.JwtAuthenticationToken
import nl.klrnbk.stuffie.api.facade.AccountFacade
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SecurityRequirement(name = "api-key")
@RequestMapping("/v1/account")
@BaseErrorResponses
class AccountController(
    private val accountFacade: AccountFacade,
) {
    @DeleteMapping
    @Operation(
        summary = "Erase everything of the signed-in user (GDPR)",
        description =
            "Permanently removes all stuffies, share links and photos and leaves one hashed tombstone. " +
                "The caller is responsible for the extra confirmation and fresh-login check, and for removing its own login records.",
    )
    @ApiResponse(
        responseCode = "204",
        description = "All data of the user has been erased",
        content = [Content(mediaType = "*/*", schema = Schema(implementation = Void::class))],
    )
    fun eraseAccount(authentication: JwtAuthenticationToken) = accountFacade.eraseAccount(authentication.principal)
}
