package com.ahelgeso.shutter.gphoto

import com.ahelgeso.shutter.ShutterAppConfig
import org.apache.logging.log4j.LogManager
import org.gphoto2.Camera
import org.gphoto2.CameraList
import org.gphoto2.CameraUtils
import org.gphoto2.GPhotoException
import org.springframework.stereotype.Component
import java.nio.file.Path
import javax.annotation.PostConstruct

/**
 * A class for interacting with the GPhoto2 library. That library just depends on calling constructors directly
 * but I want to be able to intercept these for unit testing.
 */
@Component
class GPhoto(val config: ShutterAppConfig) {
    companion object {
        private val log = LogManager.getLogger(GPhoto::class.java)
    }

    private final var _camera: Camera? = null
    final val camera: Camera
        @Synchronized
        get() = ifReadyOrNew()

    fun Camera.isReady() = isInitialized && !isClosed

    /**
     * Return the existing CameraWrapper if it's deemed ready for work or else clear all existing
     * resources and create a new newCamera.
     *
     * If a new newCamera is required and an error is received while initializing this will return a NoCamera instance.
     *
     * There's a simplifying assumption being made throughout this class that only one thread is ever trying to interact
     * with the camera at once. This isn't a very fancy photo booth and the capture API is expected to be called by
     * one frontend when in use. If you were to have multiple threads call this you could end up with two camera objects
     * in memory and one of them getting CameraUtils.closeQuietly called on it.
     */
    private fun ifReadyOrNew(): Camera =
            try {
                _camera = when (_camera?.isReady()) {
                    null -> this.newCamera()
                    true -> _camera
                    false -> {
                        log.info("Camera became not ready. Attempting to create a new connection.")
                        CameraUtils.closeQuietly(_camera)
                        this.newCamera()
                    }
                }

                _camera ?: throw AssertionError("parallel access to camera?")
            } catch (e: GPhotoException) {
                // Usually thrown if we ask for the newCamera but it's not connected
                log.warn("No newCamera or there's a problem: {}", e.message)
                throw CameraUnavailableException("Camera not ready", e)
            }

    @PostConstruct
    fun init() {
        log.info("---- Starting the GPhoto Camera Service ----")
        logCameras()
    }

    // Open methods are in order to provide test seams
    protected open fun nativeCamera() = Camera()

    protected open fun nativeCameraList() = CameraList()

    fun cameraList() = nativeCameraList().use { cameras ->
        (0 until cameras.count).map {
            val model = cameras.getModel(it)
            val port = cameras.getPort(it)

            SimpleCameraDescriptor(model, port)
        }
    }

    /**
     * Get a new WithCamera wrapper that has an initialized Camera instance internally.
     */
    private fun newCamera(): Camera {
        val newCamera = nativeCamera()
        newCamera.initialize()

        return newCamera
    }

    /**
     * Capture a photo and download it from the newCamera onto the local filesystem. If the returned
     * path is null then no photo was captured or saved. The reasons for this could either be no camera
     * attached or the camera is malfunctioning.
     *
     * @throws GPhotoException if there is an internal problem when either requesting to capture an image on the
     * camera or in the subsequent request to download and save it.
     */
    @Synchronized
    fun capturePhotoToDisk(photoPath: Path) = this.camera.let {
        val cameraImage = it.captureImage()

        log.info("Saving captured image '$cameraImage' to $photoPath")
        try {
            cameraImage.save(photoPath.normalize().toString())
        } catch (ge: GPhotoException) {
            log.warn("Problem while saving captured image", ge)
            throw ge
        } finally {
            if (config.camera.cleanAfterSave) {
                log.info("Cleaning new image off newCamera")
                cameraImage.clean()
            }
        }
    }

    private fun logCameras() {
        val cameraList = this.cameraList()

        when (cameraList.size) {
            0 -> log.info("No camera found")
            1 -> log.info("Found a single camera: ${cameraList[0]}")
            else -> {
                val desc = cameraList.joinToString(prefix = "[", postfix = "]")
                log.info("Found more than one camera: $desc")
            }
        }
    }

}
