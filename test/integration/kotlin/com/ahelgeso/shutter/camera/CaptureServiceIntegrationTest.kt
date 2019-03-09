package com.ahelgeso.shutter.camera

import assertk.catch
import com.ahelgeso.shutter.ShutterAppConfig
import com.ahelgeso.shutter.ShutterException
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

class CaptureServiceIntegrationTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `init -- capture directory is not directory -- throws exception`() {
        val aFile = tempFolder.newFile()
        val service = service(aFile.absolutePath)

        val thrown = catch { service.init() }

        assertThat(thrown)
                .isInstanceOf(ShutterException::class.java)
                .hasMessageContaining("not a directory")
    }

    @Test
    fun `init -- error making capture directory -- throws exception`() {
        val baseFolder = tempFolder.newFolder()
        val captureDir = baseFolder.resolve("capture").absolutePath

        // Delete the parent directory so that when the service goes to create
        // the directory it will get an error since it's not calling mkdirs. This
        // seemed to be the most portable and reliable way to make the mkdir call
        // fail.
        Files.delete(baseFolder.toPath())
        val service = service(captureDir)

        val thrown = catch { service.init() }

        assertThat(thrown)
                .isInstanceOf(ShutterException::class.java)
                .hasMessageContaining("Failed to create")
    }

    private fun service(photoDir: String): CaptureService {
        val config = ShutterAppConfig().apply {
            capture.photoDirectory = photoDir
        }

        return CaptureService(mock(), config)
    }
}