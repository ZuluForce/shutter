package com.ahelgeso.shutter.camera

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isNull
import assertk.assertions.startsWith
import com.ahelgeso.shutter.ArbitraryData.arbitraryLong
import com.ahelgeso.shutter.gphoto.GPhoto
import com.ahelgeso.shutter.image.ImageService
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.gphoto2.GPhotoException
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

class CaptureServiceUnitTest {

    private val gphoto: GPhoto = mock()

    @Test
    fun `takePhoto -- gphoto throws exception -- returns null path`() {
        whenever(gphoto.capturePhotoToDisk(any()))
                .thenThrow(GPhotoException("CaptureService test", -1))

        val capturedImagePath = captureService(arbitraryLong()).takePhoto()

        assert(capturedImagePath).isNull()
    }

    @Test
    fun `takePhoto -- image service provided capture directory -- calls gphoto with path prefixed by capture directory`() {
        gphotoReturningNormally()
        val captureDir = "~/.cap_server_test_1234"

        captureService(arbitraryLong(), captureDir).takePhoto()

        argumentCaptor<Path>().apply {
            verify(gphoto).capturePhotoToDisk(capture())

            assert(firstValue.toString()).startsWith(captureDir)
        }
    }

    @Test
    fun `takePhoto -- image service provided capture directory -- returns id suffix from call to gphoto`() {
        gphotoReturningNormally()

        val expectedPhotoId = arbitraryLong()

        val photoId = captureService(expectedPhotoId).takePhoto()

        assertThat(photoId).isEqualTo(expectedPhotoId)

        // This may seem like a strange assertion. The reason I care is that the photo id is central
        // to later discovering and querying the photos so it's important to me that where we
        // asked gphoto to save the photo matches what we give the caller back.
        argumentCaptor<Path>().apply {
            verify(gphoto).capturePhotoToDisk(capture())

            assert(firstValue.toString()).contains(expectedPhotoId.toString())
        }
    }

    private fun gphotoReturningNormally() {
        // Nothing to mock. This method exists for test expectation readability.
    }

    private fun captureService(photoId: Long, photoDir: String = "~/.shutter-test"): CaptureService {
        val imageName = "shutter_test_$photoId.jpeg"
        val photoInfo = ImageService.PhotoInfo(Paths.get(photoDir, imageName), photoId)

        val imgService: ImageService = mock()
        whenever(imgService.nextPhoto()).thenReturn(photoInfo)

        return CaptureService(this.gphoto, imgService)
    }
}