package com.hieutm.booksplitting.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application")
open class ApplicationConfiguration(
    var uploadDirectory: String? = null,
    var secretKey: String? = null,
    var securityProvider: String? = null,
    var fileCryptoAlgorithm: FileCryptoAlgorithm? = null
) {
    class FileCryptoAlgorithm(
        var name: String? = null,
        var content: String? = null,
        var key: String? = null
    )
}