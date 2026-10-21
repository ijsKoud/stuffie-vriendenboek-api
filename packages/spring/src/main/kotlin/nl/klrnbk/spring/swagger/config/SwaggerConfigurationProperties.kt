package nl.klrnbk.spring.swagger.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stuffie.spring.swagger")
data class SwaggerConfigurationProperties(
    val title: String,
    val description: String,
    val headerApiKeys: List<SwaggerAuthenticationConfigurationProperties> = listOf(),
)

data class SwaggerAuthenticationConfigurationProperties(
    val name: String,
    val keyId: String,
    val description: String,
)
