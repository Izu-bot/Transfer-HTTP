package com.example.transferhttp.data.server

import com.example.transferhttp.domain.model.ServerStatus
import com.example.transferhttp.domain.repository.StorageRepository
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.*
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.availableForRead
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray
import java.io.File
import java.net.NetworkInterface
import java.util.Collections
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorServer @Inject constructor(
    private val storageRepository: StorageRepository
) {

    private var serverEngine: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

    private val _status = MutableStateFlow(ServerStatus.STOPPED)
    val status: StateFlow<ServerStatus> = _status

    private val _ip = MutableStateFlow<String?>(null)
    val ip: StateFlow<String?> = _ip

    fun start(port: Int) {
        if (_status.value == ServerStatus.STARTED) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverEngine = embeddedServer(CIO, port = port, host = "0.0.0.0") {

                    install(ContentNegotiation) { json() }

                    routing {
                        get("/") {
                            call.respondText(WebInterface.HTML_INTERFACE, ContentType.Text.Html)
                        }

                        get("/api/files") {
                            val requestedPath = call.queryParameters["path"] ?: storageRepository.getRootDirPath()
                            val files = storageRepository.listFiles(requestedPath)
                            call.respond(files)
                        }

                        get("/api/download") {
                            val filePath = call.queryParameters["path"]
                            if (filePath == null) {
                                call.respond(HttpStatusCode.BadRequest, "Caminho do arquivo não informado.")
                                return@get
                            }

                            val targetFile = File(filePath)
                            if (!targetFile.exists()) {
                                call.respond(HttpStatusCode.NotFound, "O item solicitado não existe.")
                                return@get
                            }

                            if (targetFile.isDirectory) {
                                call.response.header(
                                    HttpHeaders.ContentDisposition,
                                    ContentDisposition
                                        .Attachment
                                        .withParameter(
                                            ContentDisposition.Parameters.FileName,
                                            "${targetFile.name}.zip").toString()
                                )

                                call.respondOutputStream(ContentType.Application.Zip) {
                                    ZipOutputStream(this).use { zipOut ->
                                        zipFolderHelper(targetFile, targetFile.name, zipOut)
                                    }
                                }
                            } else {
                                call.response.header(
                                    HttpHeaders.ContentDisposition,
                                    ContentDisposition
                                        .Attachment
                                        .withParameter(ContentDisposition.Parameters.FileName, targetFile.name).toString()
                                )
                                call.respondFile(targetFile)
                            }
                        }

                        post("/api/mkdir") {
                            val parentPath = call.queryParameters["parentPath"] ?: storageRepository.getRootDirPath()
                            val folderName = call.queryParameters["name"]

                            if (folderName.isNullOrBlank()) {
                                call.respond(HttpStatusCode.BadRequest, "Nome da pasta não informado.")
                                return@post
                            }

                            val success = storageRepository.createDirectory(parentPath, folderName)
                            if (success) {
                                call.respond(HttpStatusCode.OK, "Pasta criada com sucesso.")
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Erro ao criar a pasta.")
                            }
                        }

                        post("/api/upload") {
                            val targetPath = call.queryParameters["path"] ?: storageRepository.getRootDirPath()
                            val relativePath = call.queryParameters["relativePath"]

                            val multipart = call.receiveMultipart()
                            var uploadSuccess = false

                            multipart.forEachPart { part ->
                                if (part is PartData.FileItem) {
                                    val fileName = part.originalFileName ?: "arquivo_enviado"
                                    val fileBytes = part.provider().readRemaining().readByteArray()

                                    try {
                                        val destinationFile = if (!relativePath.isNullOrBlank()) {
                                            val fileLocation = File(targetPath, relativePath)
                                            fileLocation.parentFile?.mkdirs()
                                            fileLocation
                                        } else {
                                            File(targetPath, fileName)
                                        }

                                        destinationFile.writeBytes(fileBytes)
                                        uploadSuccess = true
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        uploadSuccess = false
                                    }
                                }
                                part.release()
                            }

                            if (uploadSuccess) {
                                call.respond(HttpStatusCode.OK, "Upload concluído com sucesso.")
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Erro no upload.")
                            }
                        }
                    }
                }.start(wait = false)

                _ip.value = getLocalIpAddress()
                _status.value = ServerStatus.STARTED
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = ServerStatus.ERROR
            }
        }
    }

    fun stop() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverEngine?.stop(1000, 2000)
                serverEngine = null
                _ip.value = null
                _status.value = ServerStatus.STOPPED
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterfaces in interfaces) {
                val addresses = Collections.list(networkInterfaces.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address.hostAddress!!.contains(".")) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun zipFolderHelper(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
        if (fileToZip.isHidden) return

        if (fileToZip.isDirectory) {
            val children = fileToZip.listFiles() ?: return

            if (children.isEmpty()) {
                zipOut.putNextEntry(ZipEntry("$fileName/"))
                zipOut.closeEntry()
            } else {
                for (childFile in children) {
                    zipFolderHelper(childFile, "$fileName/${childFile.name}", zipOut)
                                    }
            }
        }
        return
    }
}