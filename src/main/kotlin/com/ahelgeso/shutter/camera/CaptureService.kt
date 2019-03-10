package com.ahelgeso.shutter.camera

import com.ahelgeso.shutter.gphoto.GPhoto
import com.ahelgeso.shutter.image.ImageService
import org.apache.logging.log4j.LogManager
import org.gphoto2.GPhotoException
import org.springframework.stereotype.Service

/**
 * A service for managing photos on the filesystem and interacting with the gphoto service.
 */
@Service
class CaptureService(val gphoto: GPhoto,
                     val images: ImageService) {

    companion object {
        private val log = LogManager.getLogger(CaptureService::class.java)
    }

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
        val newPhoto = images.nextPhoto()

        return try {
            gphoto.capturePhotoToDisk(newPhoto.path)

            newPhoto.id
        } catch (e: GPhotoException) {
            log.warn("Problem capturing image to path '${newPhoto.path}'", e)
            null
        }
    }
}