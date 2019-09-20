package com.hieutm.booksplitting.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
open class FileConfiguration(
    @Value("\${application.file.upload-directory}") val uploadDir: String,
    @Value("\${application.file.crypto-algorithm.name}") val fileNameCryptoAlgorithm: String,
    @Value("\${application.file.crypto-algorithm.content}") val fileContentCryptoAlgorithm: String,
    @Value("\${application.file.crypto-algorithm.key}") val fileKeyAlgorithm: String,
    @Value("\${application.file.security-provider}") val securityprovider: String,
    @Value("\${application.file.secret-key}") val secretKey: String
)