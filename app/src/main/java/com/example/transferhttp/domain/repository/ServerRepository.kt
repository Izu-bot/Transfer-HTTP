package com.example.transferhttp.domain.repository

import com.example.transferhttp.domain.model.ServerStatus
import kotlinx.coroutines.flow.StateFlow

interface ServerRepository {
    val serverStatus: StateFlow<ServerStatus>
    val serverIp: StateFlow<String?>

    fun start(port: Int)
    fun stop()
}