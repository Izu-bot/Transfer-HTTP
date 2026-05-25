package com.example.transferhttp.domain.repository

import com.example.transferhttp.domain.model.LocalFile
import java.io.File

interface StorageRepository {
    fun getRootDirPath(): String
    fun listFiles(path: String): List<LocalFile>
    fun createDirectory(parentPath: String, folderName: String): Boolean
    fun getFile(path: String): File?
    fun saveFile(parentPath: String, fileName: String, bytes: ByteArray): Boolean
}
