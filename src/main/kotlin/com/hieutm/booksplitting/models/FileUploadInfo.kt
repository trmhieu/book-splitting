package com.hieutm.booksplitting.models

import java.io.File

class FileUploadInfo(val targetFile: File, val actualFileName: String) {

    fun getTargetFilePathWithoutExtension(): String {
        return targetFile.absolutePath.removeSuffix("." + targetFile.extension)
    }

    fun getTargetFileNameWithoutExtension(): String {
        return targetFile.nameWithoutExtension
    }
}