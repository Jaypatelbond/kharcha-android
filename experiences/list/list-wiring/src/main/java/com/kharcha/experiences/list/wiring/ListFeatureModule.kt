package com.kharcha.experiences.list.wiring

import com.kharcha.experiences.list.impl.ListFeatureImpl
import com.kharcha.experiences.list.api.ListFeatureApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ListFeatureModule {

    @Binds
    abstract fun bindListFeatureApi(
        impl: ListFeatureImpl
    ): ListFeatureApi
}
