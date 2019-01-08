package com.ahelgeso.shutter.gphoto

/**
 * A representation of a camera description that's simpler than what a
 * CameraList stores with native pointers. When returning things to the application
 * we don't want to deal with the chance of leaking memory so we save off what we
 * need and free the native memory.
 */
data class SimpleCameraDescriptor(
        val model: String,
        val port: String
)