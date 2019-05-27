package com.ahelgeso.shutter.camera

import com.ahelgeso.shutter.ShutterAppConfig
import com.ahelgeso.shutter.gphoto.GPhoto
import com.ahelgeso.shutter.image.ImageService
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path

class CaptureServiceIntegrationTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    val gphoto: GPhoto = mock()

    @Test
    fun `takePhoto -- gphoto returning normally -- returns id of new image`() {
        val outputDir = tempFolder.newFolder()
        val imgService = imageService(outputDir.absolutePath)
        val captureService = captureService(gphoto, imgService)

        val photoId = captureService.takePhoto()

        assertThat(photoId).isNotNull()

        argumentCaptor<Path> {
            verify(gphoto).capturePhotoToDisk(capture())

            assertThat(firstValue.fileName.toString()).contains(photoId.toString())
        }
    }

    private fun imageService(photoDir: String): ImageService {
        val config = ShutterAppConfig().apply {
            capture.photoDirectory = photoDir
        }

        return ImageService(config)
    }

    private fun captureService(gphoto: GPhoto, imageService: ImageService): CaptureService {

        return CaptureService(gphoto, imageService)
    }
}