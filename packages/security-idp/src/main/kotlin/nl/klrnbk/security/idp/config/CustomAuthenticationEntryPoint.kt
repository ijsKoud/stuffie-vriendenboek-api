package nl.klrnbk.security.idp.config

import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nl.klrnbk.common.exceptions.ApiException
import nl.klrnbk.common.responses.error.ErrorResponse
import nl.klrnbk.security.idp.exceptions.InvalidJwtException
import nl.klrnbk.security.idp.exceptions.JwtVerifyException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.io.Serializable

@Component
class CustomAuthenticationEntryPoint :
    AuthenticationEntryPoint,
    Serializable {
    private val log: Logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint::class.java)
    private val objectMapper = JsonMapper.builder().build()

    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        // Nothing (valid) was presented for a protected route: that is a 401, not a 403.
        if (authException !is AuthenticationServiceException) {
            return handleErrorResponse(
                request,
                response,
                authException,
                HttpStatus.UNAUTHORIZED,
            )
        }

        val rootCause: Throwable = authException.cause ?: authException
        val status =
            when (rootCause) {
                is JwtVerifyException -> HttpStatus.UNAUTHORIZED
                is InvalidJwtException -> HttpStatus.UNAUTHORIZED
                is AlgorithmMismatchException -> HttpStatus.UNAUTHORIZED
                is TokenExpiredException -> HttpStatus.UNAUTHORIZED
                is SignatureVerificationException -> HttpStatus.UNAUTHORIZED
                is com.auth0.jwt.exceptions.JWTVerificationException -> HttpStatus.UNAUTHORIZED
                else -> HttpStatus.INTERNAL_SERVER_ERROR
            }

        handleErrorResponse(request, response, rootCause, status)
    }

    @Throws(IOException::class)
    private fun handleErrorResponse(
        request: HttpServletRequest,
        response: HttpServletResponse,
        ex: Throwable,
        status: HttpStatus,
    ) {
        log.warn(
            "Error setting up security context - uri: ${request.requestURI}, status: ${status.value()}, message: ${ex.message}",
        )

        val error =
            when (ex) {
                is ApiException -> {
                    ErrorResponse(
                        status = status.value(),
                        type = status.name,
                        message = ex.message ?: "Error setting up security context",
                        detail = ex.detail,
                        instance = request.requestURI,
                    )
                }

                else ->
                    ErrorResponse(
                        status = status.value(),
                        type = status.name,
                        message = "Error setting up security context",
                        detail = ex.message ?: "Error setting up security context",
                        instance = request.requestURI,
                    )
            }

        response.status = status.value()
        response.contentType = "application/problem+json"
        response.writer.use { writer ->
            writer.write(objectMapper.writeValueAsString(error))
            writer.flush()
        }
    }
}
