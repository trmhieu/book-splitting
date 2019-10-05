package com.hieutm.booksplitting.exceptionhandler

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException

@ControllerAdvice
class FileUploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxSizeException(): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body("File too large. Maximum file size is 100MB")
    }
}