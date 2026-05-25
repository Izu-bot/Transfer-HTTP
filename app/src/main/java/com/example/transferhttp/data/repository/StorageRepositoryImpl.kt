package com.example.transferhttp.data.repository

import android.os.Environment
import com.example.transferhttp.domain.model.LocalFile
import com.example.transferhttp.domain.repository.StorageRepository
import java.io.File
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor() : StorageRepository {
    override fun getRootDirPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    override fun listFiles(path: String): List<LocalFile> {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) return emptyList()

        return directory.listFiles()?.map { file ->
            LocalFile(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isDirectory) 0L else file.length(),
                lasModified = file.lastModified()
            )
        }?.sortedWith(
            compareBy(
                { it.isDirectory },{ !it.isDirectory }, { it.name.lowercase() }
            )
        ) ?: emptyList()
    }

    override fun createDirectory(
        parentPath: String,
        folderName: String
    ): Boolean {
        val newFolder = File(parentPath, folderName)
        return if (!newFolder.exists()) {
            newFolder.mkdirs()
        } else false
    }

    override fun getFile(path: String): File? {
        val file = File(path)
        return if (file.exists() && file.isFile) file else null
    }

    override fun saveFile(
        parentPath: String,
        fileName: String,
        bytes: ByteArray
    ): Boolean {
        return try {
            val targetFile = File(parentPath, fileName)
            targetFile.writeBytes(bytes)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}