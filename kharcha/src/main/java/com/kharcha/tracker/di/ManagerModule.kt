package com.kharcha.tracker.di

import com.kharcha.core.domain.manager.ReminderManager
import com.kharcha.tracker.manager.ReminderManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Binds
    @Singleton
    abstract fun bindReminderManager(
        impl: ReminderManagerImpl
    ): ReminderManager
}
