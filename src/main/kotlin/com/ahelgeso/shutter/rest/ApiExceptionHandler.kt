package com.ahelgeso.shutter.rest

import com.ahelgeso.shutter.gphoto.CameraUnavailableException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(CameraUnavailableException::class)
    fun handleCameraUnavailable(): String {
        return "Camera is unavailable or not functioning"
    }
}