package nl.klrnbk.stuffie.config

import nl.klrnbk.stuffie.storage.LocalPhotoStorage
import nl.klrnbk.stuffie.storage.PhotoStorage
import nl.klrnbk.stuffie.storage.S3PhotoStorage
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(StuffieProperties::class)
class StorageConfiguration {
    @Bean
    fun photoStorage(properties: StuffieProperties): PhotoStorage =
        when (val driver = properties.storage.driver) {
            "local" -> LocalPhotoStorage(properties.storage.localDir)
            "s3" -> S3PhotoStorage(properties.storage.s3)
            else -> error("Unknown stuffie.storage.driver '$driver' (expected 'local' or 's3')")
        }
}
