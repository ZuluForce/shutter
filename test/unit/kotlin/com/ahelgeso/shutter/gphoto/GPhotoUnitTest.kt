package com.ahelgeso.shutter.gphoto

import assertk.assert
import assertk.assertions.*
import assertk.catch
import com.ahelgeso.shutter.ArbitraryData
import com.ahelgeso.shutter.ShutterAppConfig
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.gphoto2.Camera
import org.gphoto2.CameraFile
import org.gphoto2.CameraList
import org.gphoto2.GPhotoException
import org.junit.Test
import java.nio.file.Paths

class GPhotoUnitTest {

    @Test
    fun `cameraList -- no cameras -- returns empty set`() {
        val gphoto = withNoCameras()

        val cameraList = gphoto.cameraList()

        assert(cameraList).isEmpty()
    }

    @Test
    fun `cameraList -- with camera -- returns list with camera`() {
        val model = "Canon Test Model ${ArbitraryData.arbitraryInt()}"
        val port = ArbitraryData.arbitraryString()
        val (gphoto, _) = withCamera(model, port)

        val cameraList = gphoto.cameraList()

        assert(cameraList).hasSize(1)
        assert(cameraList).index(0) {
            it.prop(SimpleCameraDescriptor::model).isEqualTo(model)
            it.prop(SimpleCameraDescriptor::port).isEqualTo(port)
        }
    }

    @Test
    fun `camera -- native call throws exception -- wrapper throws`() {
        val gphoto = cameraConstructorThrows()

        val thrown = catch { gphoto.camera }

        assert(thrown).isNotNull {
            it.isInstanceOf(CameraUnavailableException::class.java)
        }
    }

    @Test
    fun `camera -- camera initialization throws exception -- camera method throws`() {
        val gphoto = cameraInitThrows()

        val thrown = catch { gphoto.camera }

        assert(thrown).isNotNull {
            it.isInstanceOf(CameraUnavailableException::class.java)
        }
    }

    @Test
    fun `camera -- new camera provisioned -- init called on camera`() {
        val (gphoto, expectedNewCamera) = withCamera()

        val returnedCamera = gphoto.camera

        assert(returnedCamera).isNotNull()
        verify(expectedNewCamera).initialize()
    }

    @Test
    fun `camera -- existing is not ready -- new camera created`() {
        // Always return a new camera mock anytime we're asked to
        val gphoto = withCamera(cameraFn = { mock() })
        // By calling the getter we get the field populated and ready for the next call
        // where the not ready status is detected. This is a design smell in my opinion but
        // I'm bound to how the native library is implemented.
        val originalCamera = gphoto.camera
        whenever(originalCamera.isClosed).thenReturn(true)

        // This should see the original as not ready and create a new one
        val newCamera = gphoto.camera

        assert(newCamera).isNotSameAs(originalCamera)
        verify(originalCamera).close()
    }

    @Test
    fun `capturePhotoToDisk -- save gives error and clean is true -- calls clean on camera image`() {
        val (gphoto, cameraFile) = cameraThrowingOnSave()
        gphoto.config.camera.cleanAfterSave = true
        val photoDir = Paths.get("~/.test")

        val thrown = catch { gphoto.capturePhotoToDisk(photoDir) }

        assert(thrown).isNotNull()
        verify(cameraFile).clean()
    }

    @Test
    fun `capturePhotoToDisk -- save gives error and clean is false -- clean never called`() {
        val (gphoto, cameraFile) = cameraThrowingOnSave()
        gphoto.config.camera.cleanAfterSave = false
        val photoPath = Paths.get("~/.test/${ArbitraryData.arbitraryInt()}.jpg")

        val thrown = catch { gphoto.capturePhotoToDisk(photoPath) }

        assert(thrown).isNotNull()
        verify(cameraFile, never()).clean()
    }

    @Test
    fun `capturePhotoToDisk -- save successful clean is true -- clean is called`() {
        val (gphoto, cameraFile) = cameraSaveSuccessful()
        gphoto.config.camera.cleanAfterSave = true
        val photoPath = Paths.get("~/.test/${ArbitraryData.arbitraryInt()}.jpg")

        gphoto.capturePhotoToDisk(photoPath)

        verify(cameraFile).clean()
    }

    private fun cameraThrowingOnSave(): Pair<GPhoto, CameraFile> {
        val (gphoto, camera) = withCamera()
        val cameraFile: CameraFile = mock()
        whenever(camera.captureImage()).thenReturn(cameraFile)
        whenever(cameraFile.save(any())).thenThrow(GPhotoException("Test error saving image", -1))

        return Pair(gphoto, cameraFile)
    }

    private fun cameraSaveSuccessful(): Pair<GPhoto, CameraFile> {
        val (gphoto, camera) = withCamera()
        val cameraFile: CameraFile = mock()
        whenever(camera.captureImage()).thenReturn(cameraFile)

        return Pair(gphoto, cameraFile)
    }

    /**
     * Return no cameras in the CameraList.
     */
    private fun withNoCameras(): GPhoto {
        val cameraList: CameraList = mock()
        whenever(cameraList.count).thenReturn(0)
        return testableGphoto(cameraListFn = { cameraList })
    }

    /**
     * Return a camera in the CameraList and also if Camera is called.
     */
    private fun withCamera(model: String = "Canon Test Model",
                           port: String = "usb: 0,1"): Pair<GPhoto, Camera> {
        val cameraList = cameraList(listOf(model), listOf(port))
        val camera: Camera = mock()

        val gphoto = testableGphoto(
                cameraFn = { camera },
                cameraListFn = { cameraList })

        return Pair(gphoto, camera)
    }

    private fun withCamera(cameraFn: () -> Camera,
                           cameraListFn: (() -> CameraList)? = null): GPhoto =
            testableGphoto(
                    cameraFn = cameraFn,
                    cameraListFn = cameraListFn ?: { cameraList() })

    private fun cameraList(models: List<String> = listOf("Canon Test Model"),
                           ports: List<String> = listOf("usb: 0,1")): CameraList {
        assertThat(models).hasSameSizeAs(ports)
        val cameraList: CameraList = mock()
        whenever(cameraList.getPort(any())).thenAnswer {
            val portIndex: Int = it.getArgument(0)
            ports[portIndex]
        }
        whenever(cameraList.getModel(any())).thenAnswer {
            val modelIndex: Int = it.getArgument(0)
            models[modelIndex]
        }
        whenever(cameraList.count).thenReturn(models.size)

        return cameraList
    }

    /**
     * Throw a GPhotoException when the GPhoto#camera method is called.
     */
    private fun cameraConstructorThrows() =
            testableGphoto(cameraFn = {
                throw GPhotoException("Expected test exception", -1)
            })

    private fun cameraInitThrows(): GPhoto {
        val camera: Camera = mock()
        whenever(camera.initialize()).thenThrow(GPhotoException("Test Exception during init", -1))
        return testableGphoto(cameraFn = { camera })
    }

    /**
     * Allow overriding of the two main external interfacing points used by our
     * GPhoto service.
     */
    private fun testableGphoto(
            cameraFn: () -> Camera = mock(),
            cameraListFn: () -> CameraList = mock()
    ) = object : GPhoto(ShutterAppConfig()) {
        override fun nativeCamera() = cameraFn.invoke()

        override fun nativeCameraList() = cameraListFn.invoke()
    }
}