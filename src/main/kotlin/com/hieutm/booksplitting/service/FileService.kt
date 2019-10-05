package com.hieutm.booksplitting.service

import com.hieutm.booksplitting.configuration.ApplicationConfiguration
import com.hieutm.booksplitting.models.FileUploadInfo
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@Service
class FileService(
    private val applicationConfiguration: ApplicationConfiguration,
    private val encryptionDecryptionService: EncryptionDecryptionService
) {
    private var uploadDir: Path

    init {
        try {
            uploadDir = Files.createDirectories(
                Paths.get(applicationConfiguration.uploadDirectory!!).toAbsolutePath().normalize()
            )
        } catch (e: Exception) {
            throw Exception("Error during create upload directory: ${e.message}")
        }
    }

    fun buildFileInfo(file: MultipartFile): FileUploadInfo {
        val fileName = StringUtils.cleanPath(file.originalFilename!!)
        var encryptedFileName = encryptionDecryptionService
            .encryptFileName(fileName + LocalDateTime.now(), applicationConfiguration.fileCryptoAlgorithm!!.name!!)
        encryptedFileName = encryptedFileName?.plus(".zip")
            ?: throw Exception("Encrypting file name for $fileName failed")
        return FileUploadInfo(uploadDir.resolve(encryptedFileName).toFile(), fileName)
    }

    fun splitFile(file: MultipartFile, numberOfParts: Int): List<ByteArray> {
        try {
            val doc: PDDocument = PDDocument.load(file.inputStream)
            val numberPageEachPart = (doc.numberOfPages / numberOfParts)
                .takeIf { it != 0 }
                .let { it } ?: 1
            val splitter = Splitter()
            splitter.setSplitAtPage(numberPageEachPart)
            val bytes = splitter.split(doc).map { pdDocument ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                pdDocument.save(byteArrayOutputStream)
                pdDocument.close()
                byteArrayOutputStream.toByteArray()
            }
            doc.close()
            return bytes
        } catch (ex: IOException) {
            throw Exception("Split file ${file.originalFilename} failed: $ex")
        }

    }

    fun zipFile(fileUploadInfo: FileUploadInfo, numberOfParts: Int, bytes: List<ByteArray>) {
        val buffer = ByteArray(1024)
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val zos = ZipOutputStream(byteArrayOutputStream)
            for (index in 0..(numberOfParts - 1)) {
                val ze = ZipEntry("${fileUploadInfo.actualFileName}_${(index + 1)}.pdf")
                zos.putNextEntry(ze)
                val fis = ByteArrayInputStream(bytes[index])
                var len = 0
                while ({ len = fis.read(buffer); len }() > 0) {
                    zos.write(buffer, 0, len)
                }
                zos.closeEntry()
                fis.close()
            }
            zos.close()
            writeFile(byteArrayOutputStream, fileUploadInfo.targetFile)
        } catch (e: Exception) {
            throw Exception("Error during zip and write file: ${e.message}")
        }
    }

    private fun writeFile(byteArrayOutputStream: ByteArrayOutputStream, targetFile: File) {
        val encryptedFileContent = encryptionDecryptionService.encryptFileContent(
            ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
            byteArrayOutputStream.toByteArray().size
        )
        val fileOutputStream = FileOutputStream(targetFile)
        fileOutputStream.write(encryptedFileContent)
        fileOutputStream.close()
    }


    fun getFileContent(storedFileName: String): ByteArray {
        val encryptedFile = this.uploadDir.resolve(storedFileName).normalize().toFile()
        val fileInputStream: FileInputStream
        try {
            fileInputStream = FileInputStream(encryptedFile)
        } catch (ex: IOException) {
            throw FileNotFoundException("File not found $storedFileName: $ex")
        }
        return encryptionDecryptionService.decryptFileContent(
            fileInputStream,
            encryptedFile.length().toInt()
        )
    }
}