package com.ahelgeso.shutter.camera

import com.ahelgeso.shutter.ShutterAppConfig
import com.ahelgeso.shutter.ShutterException
import com.ahelgeso.shutter.gphoto.GPhoto
import org.apache.logging.log4j.LogManager
import org.gphoto2.GPhotoException
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import javax.annotation.PostConstruct

/**
 * A service for managing photos on the filesystem and interacting with the gphoto service.
 */
@Service
class CaptureService(val gphoto: GPhoto,
                     val config: ShutterAppConfig) {

    companion object {
        private val log = LogManager.getLogger(CaptureService::class.java)

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
     * Take a photo on the connected camera and save it to a local directory. If there's
     * any problem with the camera or there's an issue downloading and saving to the local filesystem
     * then you will receive back a null photo ID.
     *
     * This method is synchronized since the camera can only take one photo at a time.
     *
     * TODO: Is synchronizing enough here? Does the camera need some extra time to prepare itself for the next image capture? Does
     * this even matter since people shouldn't be taking photos too quickly?
     */
    @Synchronized
    fun takePhoto(): Long? {
        val (photoName, photoId) = nextPhotoNameAndId()
        val photoPath = Paths.get(config.capture.photoDirectory, photoName)

        return try {
            gphoto.capturePhotoToDisk(photoPath)

            photoId
        } catch (e: GPhotoException) {
            log.warn("Problem capturing image", e)
            null
        }
    }

    private fun nextPhotoNameAndId(): Pair<String, Long> {
        // Yes, not guaranteed to be unique between calls but let's be real, the time it takes
        // between photo captures on the camera is definitely hundreds to thousands of milliseconds.
        val photoId = System.currentTimeMillis()
        val photoName = photoNameFromId(photoId)

        return Pair(photoName, photoId)
    }

    private fun photoNameFromId(photoId: Long) = "${photoPrefix}_$photoId.${config.capture.photoExtension}"
}