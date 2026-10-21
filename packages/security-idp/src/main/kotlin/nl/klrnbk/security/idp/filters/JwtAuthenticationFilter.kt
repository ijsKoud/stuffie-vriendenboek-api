package nl.klrnbk.security.idp.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nl.klrnbk.security.idp.config.CustomAuthenticationEntryPoint
import nl.klrnbk.security.idp.exceptions.InvalidJwtException
import nl.klrnbk.security.idp.helpers.resolveJwtFromRequest
import nl.klrnbk.security.idp.models.JwtAuthenticationToken
import nl.klrnbk.security.idp.service.OpenIdService
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    val openIdService: OpenIdService,
    val authenticationEntryPoint: CustomAuthenticationEntryPoint,
) : OncePerRequestFilter() {
    private val securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()
    private val securityContextRepository = RequestAttributeSecurityContextRepository()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveJwtFromRequest(request)
        if (token.isNullOrBlank()) return filterChain.doFilter(request, response)

        val decoded =
            try {
                val decoded = openIdService.decodeAndVerifyJwt(token)
                if (decoded == null) throw InvalidJwtException()

                decoded
            } catch (exception: Exception) {
                val authException = AuthenticationServiceException(exception.message, exception)
                authenticationEntryPoint.commence(request, response, authException)

                return
            }

        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = JwtAuthenticationToken(decoded, openIdService)

        securityContextHolderStrategy.context = context
        securityContextRepository.saveContext(context, request, response)

        filterChain
            .doFilter(request, response)
    }
}
