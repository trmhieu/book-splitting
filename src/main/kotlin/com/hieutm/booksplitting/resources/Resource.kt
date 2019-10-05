package com.hieutm.booksplitting.resources

import com.hieutm.booksplitting.service.FileService
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
class Resource(private val fileService: FileService) {

    @PostMapping("/upload")
    fun upload(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("parts") numberOfParts: Int
    ): ResponseEntity<Any> {
        val bytes = fileService.splitFile(file, numberOfParts)
        val fileInfo = fileService.buildFileInfo(file)
        fileService.zipFile(fileInfo, numberOfParts, bytes)
        val location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/download/")
            .path(fileInfo.getTargetFileNameWithoutExtension())
            .build()
            .toUri()
        return ResponseEntity.created(location).build()
    }

    @GetMapping("/download/{id}", produces = ["application/zip"])
    fun download(@PathVariable("id") fileId: String): ResponseEntity<Resource> {
        val headers = HttpHeaders()
        val fileBytes = fileService.getFileContent(fileId.plus(".zip"))
        val contentDisposition = ContentDisposition.builder("attachment").filename(fileId.plus(".zip")).build()
        headers.contentDisposition = contentDisposition
        val resource = ByteArrayResource(fileBytes)
        return ResponseEntity.ok().body(resource)
    }

}