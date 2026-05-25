package com.example.transferhttp.data.repository

import com.example.transferhttp.data.server.KtorServer
import com.example.transferhttp.domain.model.ServerStatus
import com.example.transferhttp.domain.repository.ServerRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ServerRepositoryImpl @Inject constructor(
    private val ktorServer: KtorServer
) : ServerRepository {
    override val serverStatus: StateFlow<ServerStatus> = ktorServer.status
    override val serverIp: StateFlow<String?> = ktorServer.ip

    override fun start(port: Int) {
        ktorServer.start(port)
    }

    override fun stop() {
        ktorServer.stop()
    }
}