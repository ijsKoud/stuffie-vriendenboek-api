package nl.klrnbk.spring

import io.swagger.v3.oas.annotations.Hidden
import nl.klrnbk.common.exceptions.ApiException
import nl.klrnbk.common.responses.error.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Hidden
@ControllerAdvice
class GlobalControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)

    @ExceptionHandler(ApiException::class)
    fun handleApiExceptions(ex: ApiException): ResponseEntity<ErrorResponse> =
        problem(
            status = ex.status,
            message = ex.message!!,
            detail = ex.detail,
            instance = ex.instance,
        )

    @ExceptionHandler(Exception::class)
    fun handleExceptions(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(ex.stackTraceToString())

        return problem(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = "An unexpected error occurred.",
            detail = ex.message ?: "An unexpected error occurred.",
            instance = ApiException.getExceptionInstance(),
        )
    }

    /**
     * Spring's own MVC errors (validation, unreadable body, 404, 405, upload too large, ...) keep their status
     * but are rendered in our [ErrorResponse] shape.
     */
    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val status = HttpStatus.resolve(statusCode.value()) ?: HttpStatus.INTERNAL_SERVER_ERROR
        val detail = (body as? ProblemDetail)?.detail ?: ex.message ?: status.reasonPhrase
        val response = problem(status, status.reasonPhrase, detail, ApiException.getExceptionInstance())

        @Suppress("UNCHECKED_CAST")
        return response as ResponseEntity<Any>
    }

    private fun problem(
        status: HttpStatus,
        message: String,
        detail: String,
        instance: String,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(status.value())
            .header(HttpHeaders.CONTENT_TYPE, "application/problem+json")
            .body(
                ErrorResponse(
                    status = status.value(),
                    type = status.name,
                    message = message,
                    detail = detail,
                    instance = instance,
                ),
            )
}
