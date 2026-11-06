package nl.klrnbk.common.responses.error

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    content = [
        Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(value = ErrorResponse.BAD_REQUEST)],
        ),
    ],
)
annotation class BadRequestResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "401",
    content = [
        Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(value = ErrorResponse.UNAUTHORIZED)],
        ),
    ],
)
annotation class UnauthorizedResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    content = [
        Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(value = ErrorResponse.NOT_FOUND)],
        ),
    ],
)
annotation class NotFoundResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "500",
    content = [
        Content(
            mediaType = "application/problem+json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(value = ErrorResponse.INTERNAL_SERVER_ERROR)],
        ),
    ],
)
annotation class InternalServerErrorResponse

@UnauthorizedResponse
@InternalServerErrorResponse
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseErrorResponses
