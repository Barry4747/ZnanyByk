package com.example.myapplication.di

import com.example.myapplication.utils.DefaultStringsProvider
import com.example.myapplication.utils.StringsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StringsModule {
    @Binds
    @Singleton
    abstract fun bindStringsProvider(impl: DefaultStringsProvider): StringsProvider
}

