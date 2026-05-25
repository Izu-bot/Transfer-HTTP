package com.example.transferhttp.ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.transferhttp.domain.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val serverRepository: ServerRepository
): ViewModel() {
    val serverStatus = serverRepository.serverStatus
    val serverIp = serverRepository.serverIp
}