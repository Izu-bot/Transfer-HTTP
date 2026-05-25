package com.example.transferhttp.di

import com.example.transferhttp.data.repository.ServerRepositoryImpl
import com.example.transferhttp.data.repository.StorageRepositoryImpl
import com.example.transferhttp.domain.repository.ServerRepository
import com.example.transferhttp.domain.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindServerRepository(
        serverRepositoryImpl: ServerRepositoryImpl
    ): ServerRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository
}