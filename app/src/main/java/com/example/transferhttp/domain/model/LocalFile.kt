package com.example.transferhttp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lasModified: Long
)