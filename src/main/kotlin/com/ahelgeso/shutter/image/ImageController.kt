package com.ahelgeso.shutter.image

import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("image")
class ImageController(val images: ImageService) {

    @GetMapping("{imageId}")
    fun getImageById(@PathVariable imageId: Long): ResponseEntity<InputStreamResource> {
        val info = images.photoInfoFromIdIfExists(imageId) ?: return ResponseEntity.notFound().build()
        val fileSize = info.length()
        val stream = info.inputStream()
        val mimeType = MediaType.parseMediaType(info.mime())

        return ResponseEntity.ok()
                .contentLength(fileSize)
                .contentType(mimeType)
                .body(InputStreamResource(stream))
    }
}