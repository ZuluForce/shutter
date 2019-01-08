package com.ahelgeso.shutter.camera

import com.ahelgeso.shutter.gphoto.CameraUnavailableException
import org.apache.logging.log4j.LogManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/camera")
class CameraController(private val cameraService: CaptureService) {

    companion object {
        private val log = LogManager.getLogger(CameraController::class.java)
    }

    @GetMapping("/capture")
    fun captureImage(): CaptureImageResponse {
        val photoId = cameraService.takePhoto()

        if (photoId == null)
            throw CameraUnavailableException("Camera not available")
        else {
            log.info("Captured photo with id '$photoId'")
            return CaptureImageResponse(photoId)
        }
    }

    data class CaptureImageResponse(val photoId: Long)
}