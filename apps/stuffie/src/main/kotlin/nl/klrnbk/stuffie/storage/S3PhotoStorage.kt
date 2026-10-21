package nl.klrnbk.stuffie.storage

import nl.klrnbk.stuffie.config.S3Properties
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI

class S3PhotoStorage(
    private val properties: S3Properties,
) : PhotoStorage {
    private val client: S3Client =
        S3Client
            .builder()
            .endpointOverride(URI.create(properties.endpoint))
            .region(Region.of(properties.region))
            .forcePathStyle(properties.forcePathStyle)
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey, properties.secretKey)),
            )
            // Garage (and most S3-compatible servers) do not implement the SDK's default CRC checksums.
            .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
            .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
            .build()

    override fun put(
        key: String,
        body: ByteArray,
    ) {
        client.putObject(
            PutObjectRequest
                .builder()
                .bucket(properties.bucket)
                .key(key)
                .contentType("image/jpeg")
                .build(),
            RequestBody.fromBytes(body),
        )
    }

    override fun get(key: String): ByteArray? =
        try {
            client
                .getObjectAsBytes(GetObjectRequest.builder().bucket(properties.bucket).key(key).build())
                .asByteArray()
        } catch (_: NoSuchKeyException) {
            null
        }

    override fun delete(key: String) {
        client.deleteObject(DeleteObjectRequest.builder().bucket(properties.bucket).key(key).build())
    }

    override fun deletePrefix(prefix: String) {
        require(prefix.isNotBlank()) { "Refusing to delete an empty prefix" }

        client
            .listObjectsV2Paginator(ListObjectsV2Request.builder().bucket(properties.bucket).prefix(prefix).build())
            .forEach { page ->
                val objects = page.contents().map { ObjectIdentifier.builder().key(it.key()).build() }
                if (objects.isEmpty()) return@forEach

                client.deleteObjects(
                    DeleteObjectsRequest
                        .builder()
                        .bucket(properties.bucket)
                        .delete(Delete.builder().objects(objects).build())
                        .build(),
                )
            }
    }
}
