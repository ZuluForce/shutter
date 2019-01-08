package com.ahelgeso.shutter.camera

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isNull
import com.ahelgeso.shutter.gphoto.GPhoto
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.gphoto2.GPhotoException
import org.junit.Test

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
    fun `takePhoto -- camera present with path returned from gphoto -- returns photo path`() {
        gphotoReturningNormally()

        val capturedImagePath = captureService(photoDir).takePhoto()

        assert(capturedImagePath.toString()).contains(photoDir)
    }

    private fun gphotoThrowingExceptionOnCapture() {
        whenever(gphoto.capturePhotoToDisk(any()))
                .thenThrow(GPhotoException("CaptureService test", -1))
    }

    private fun gphotoReturningNormally() {
        // Nothing to mock
    }


    private fun captureService(photoDir: String): CaptureService {
        val config = CaptureServiceConfig().apply {
            this.photoDirectory = photoDir
        }

        return CaptureService(this.gphoto, config)
    }
}