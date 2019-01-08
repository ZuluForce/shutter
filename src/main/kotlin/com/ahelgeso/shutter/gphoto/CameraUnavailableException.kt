package com.ahelgeso.shutter.gphoto

class CameraUnavailableException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}