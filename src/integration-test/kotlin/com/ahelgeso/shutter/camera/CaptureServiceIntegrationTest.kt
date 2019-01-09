package com.ahelgeso.shutter.camera

import assertk.catch
import com.ahelgeso.shutter.ShutterAppConfig
import com.ahelgeso.shutter.ShutterException
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CaptureServiceIntegrationTest {

    @Rule
    @JvmField
    val captureFolder = TemporaryFolder()

    @Test
    fun `init -- capture directory does not exist -- throws exception`() {
        val service = service("/a/non/existant/path")

        val thrown = catch { service.init() }

        assertThat(thrown).isInstanceOf(ShutterException::class.java)
    }

    private fun service(photoDir: String): CaptureService {
        val config = ShutterAppConfig().apply {
            capture.photoDirectory = photoDir
        }

        return CaptureService(mock(), config)
    }
}