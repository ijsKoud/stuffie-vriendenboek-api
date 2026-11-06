package nl.klrnbk.stuffie.helpers

import net.coobird.thumbnailator.Thumbnails
import nl.klrnbk.common.exceptions.BadRequestException
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

const val MAX_DIMENSION = 1200

/** Pixels above this are refused before decoding, so a small file cannot expand into gigabytes of memory. */
private const val MAX_PIXELS = 50_000_000L

@Component
class ImageProcessor {
    /**
     * Re-encodes any uploaded image as a JPEG of at most [MAX_DIMENSION] px, which drops all metadata (EXIF/GPS)
     * and applies the EXIF orientation.
     *
     * shortcut: transparent PNGs get a black background and only formats ImageIO reads (JPEG/PNG/GIF/BMP) work;
     * add a flatten step / a WebP reader when users hit it.
     */
    fun toJpeg(input: ByteArray): ByteArray {
        val (width, height) = readDimensions(input)
        val tooBig = maxOf(width, height) > MAX_DIMENSION

        val out = ByteArrayOutputStream()
        val builder = Thumbnails.of(ByteArrayInputStream(input))
        if (tooBig) builder.size(MAX_DIMENSION, MAX_DIMENSION) else builder.scale(1.0)
        builder
            .imageType(BufferedImage.TYPE_INT_RGB)
            .outputFormat("jpg")
            .outputQuality(0.82)
            .toOutputStream(out)

        return out.toByteArray()
    }

    private fun readDimensions(input: ByteArray): Pair<Int, Int> {
        val notAnImage = BadRequestException("That does not look like an image.")

        val stream = ImageIO.createImageInputStream(ByteArrayInputStream(input)) ?: throw notAnImage
        stream.use {
            val reader = ImageIO.getImageReaders(it).asSequence().firstOrNull() ?: throw notAnImage
            try {
                reader.input = it
                val width = reader.getWidth(0)
                val height = reader.getHeight(0)
                if (width.toLong() * height > MAX_PIXELS) throw BadRequestException("Image dimensions are too large.")

                return width to height
            } catch (_: java.io.IOException) {
                throw notAnImage
            } finally {
                reader.dispose()
            }
        }
    }
}
