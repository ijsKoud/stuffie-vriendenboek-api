package nl.klrnbk.spring.swagger.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@AutoConfiguration
@EnableConfigurationProperties(SwaggerConfigurationProperties::class)
class SwaggerConfiguration(
    private val properties: SwaggerConfigurationProperties,
) : WebMvcConfigurer {
    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/").setViewName(
            "redirect:/swagger-ui.html",
        )
    }

    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                getApiInfo(),
            ).components(
                getComponents(),
            )

    private fun getApiInfo(): Info =
        Info()
            .title(properties.title)
            .description(properties.description)

    private fun getComponents(): Components =
        Components().let {
            properties.headerApiKeys.forEach { headerApiKey ->
                it.addSecuritySchemes(
                    headerApiKey.keyId,
                    SecurityScheme()
                        .description(headerApiKey.description)
                        .name(headerApiKey.name)
                        .`in`(SecurityScheme.In.HEADER)
                        .type(SecurityScheme.Type.APIKEY),
                )
            }

            it
        }
}
