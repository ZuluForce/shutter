package com.ahelgeso.shutter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("shutter")
class ShutterAppConfig {
    val camera = GPhotoConfig()
    val capture = CaptureServiceConfig()
}

class CaptureServiceConfig {
    lateinit var photoDirectory: String
    var photoExtension: String = "jpeg"
}

class GPhotoConfig {
    var cleanAfterSave: Boolean = false
}
