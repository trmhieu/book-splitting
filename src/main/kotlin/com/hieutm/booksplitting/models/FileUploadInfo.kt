package com.hieutm.booksplitting.models

import java.io.File

class FileUploadInfo(val targetFile: File, val actualFileName: String) {

    fun getTargetFileNameWithoutExtension(): String {
        return targetFile.nameWithoutExtension
    }
}