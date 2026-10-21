package nl.klrnbk.security.idp.constants

val DEFAULT_UNAUTHENTICATED_ROUTES =
    listOf(
        "/",
        "/actuator/health/**",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/swagger-resources",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/swagger-ui/**",
        "/webjars/**",
        "/swagger-ui.html",
        "/favicon.ico",
    )
