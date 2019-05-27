package com.ahelgeso.shutter.image

import com.ahelgeso.shutter.ShutterAppConfig
import com.ahelgeso.shutter.ShutterException
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.PostConstruct

@Service
class ImageService(val config: ShutterAppConfig) {

    companion object {
        private val log = LogManager.getLogger(ImageService::class.java)
        private val validExtensions = setOf("jpeg", "jpg", "png")
        private const val photoPrefix = "shutter"
    }

    @PostConstruct
    fun init() {
        if (!validExtensions.contains(config.capture.photoExtension)) {
            throw ShutterException("Invalid photo extension: ${config.capture.photoExtension}")
        }

        File(config.capture.photoDirectory).apply {
            if (!exists()) {
                log.info("Photo directory $this does not exist. Creating now...")
                val success = mkdir()

                if (success)
                    log.debug("Successfully created photo directory")
                else
                    throw ShutterException("Failed to create photo directory")
            } else if (!isDirectory)
                throw ShutterException("Configured photo directory exists but is not a directory")
        }

        val photoCount = photoIterator().count()
        if (photoCount > 0) log.info("Found $photoCount existing photos")
    }

    fun photoIterator() =
            File(config.capture.photoDirectory).walk().apply {
                maxDepth(1) // Only files in this directory
            }
                    .filter { it.isFile }
                    .filter { it.name.startsWith(photoPrefix) }

    /**
     * Get a PhotoInfo if the photo with the specified ID exists.
     */
    fun photoInfoFromIdIfExists(photoId: Long): PhotoInfo? = photoPathFromId(photoId)
            .takeIf {
                it.toFile().exists()
            }?.let {
                PhotoInfo(it, photoId)
            }

    /**
     * Get a PhotoInfo representing a photo with this ID, irrespective of its presence on disk.
     */
    fun photoInfoFromId(photoId: Long): PhotoInfo = PhotoInfo(photoPathFromId(photoId), photoId)

    /**
     * Generate an ID for the next photo we capture. This does not provision anything
     * on the filesystem.
     */
    fun nextPhoto(): PhotoInfo {
        // Yes, not guaranteed to be unique between calls but let's be real, the time it takes
        // between photo captures on the camera is definitely hundreds to thousands of milliseconds.
        val photoId = System.currentTimeMillis()

        return photoInfoFromId(photoId)
    }

    private fun photoPathFromId(photoId: Long): Path = Paths.get(config.capture.photoDirectory, photoNameFromId(photoId))

    private fun photoNameFromId(photoId: Long) = "${photoPrefix}_$photoId.${config.capture.photoExtension}"

    class PhotoInfo(val path: Path,
                    val id: Long) {
        fun inputStream() = BufferedInputStream(FileInputStream(path.toFile()))
        fun length() = Files.size(path)

        // TODO: Parse the mime type from the filename
        fun mime() = "image/jpeg"
    }
}