package com.ahelgeso.shutter.camera

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isNull
import assertk.assertions.startsWith
import com.ahelgeso.shutter.ShutterAppConfig
import com.ahelgeso.shutter.gphoto.GPhoto
import com.nhaarman.mockitokotlin2.*
import org.gphoto2.GPhotoException
import org.junit.Test
import java.nio.file.Path

class CaptureServiceUnitTest {

    private val gphoto: GPhoto = mock()
    private val photoDir = "~/.test-dir"

    @Test
    fun `takePhoto -- gphoto throws exception -- returns null path`() {
        gphotoThrowingExceptionOnCapture()

        val capturedImagePath = captureService(photoDir).takePhoto()

        assert(capturedImagePath).isNull()
    }

    @Test
    fun `takePhoto -- config provided capture directory -- calls gphoto with path prefixed by capture directory`() {
        gphotoReturningNormally()

        captureService(photoDir).takePhoto()

        argumentCaptor<Path>().apply {
            verify(gphoto).capturePhotoToDisk(capture())

            assert(firstValue.toString()).startsWith(photoDir)
        }
    }

    @Test
    fun `takePhoto -- config provided capture directory -- returns id suffix from call to gphoto`() {
        gphotoReturningNormally()

        val photoId = captureService(photoDir).takePhoto()

        // This may seem like a strange assertion. The reason I care is that the photo id is central
        // to later discovering and querying the photos so it's important to me that where we
        // asked gphoto to save the photo matches what we give the caller back.
        argumentCaptor<Path>().apply {
            verify(gphoto).capturePhotoToDisk(capture())

            assert(firstValue.toString()).contains(photoId.toString())
        }
    }

    private fun gphotoThrowingExceptionOnCapture() {
        whenever(gphoto.capturePhotoToDisk(any()))
                .thenThrow(GPhotoException("CaptureService test", -1))
    }

    private fun gphotoReturningNormally() {
        // Nothing to mock
    }

    private fun captureService(photoDir: String): CaptureService {
        val config = ShutterAppConfig().apply {
            this.capture.photoDirectory = photoDir
        }

        return CaptureService(this.gphoto, config)
    }
}