package com.kharcha.experiences.history.wiring

import com.kharcha.experiences.history.api.HistoryFeatureApi
import com.kharcha.experiences.history.impl.HistoryFeatureImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryFeatureModule {

    @Binds
    @Singleton
    abstract fun bindHistoryFeatureApi(
        impl: HistoryFeatureImpl
    ): HistoryFeatureApi
}
