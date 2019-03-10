package com.ahelgeso.shutter.image

import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("image")
class ImageController(val images: ImageService) {

    @GetMapping("{imageId}")
    fun getImageById(@PathVariable imageId: Long): ResponseEntity<InputStreamResource> {
        val info = images.photoInfoFromId(imageId)
        val fileSize = info.length()
        val stream = info.inputStream()
        val mimeType = MediaType.parseMediaType(info.mime())

        return ResponseEntity.ok()
                .contentLength(fileSize)
                .contentType(mimeType)
                .body(InputStreamResource(stream))
    }
}