package com.kharcha.experiences.dashboard.wiring

import com.kharcha.experiences.dashboard.api.DashboardFeatureApi
import com.kharcha.experiences.dashboard.impl.DashboardFeatureImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardFeatureModule {

    @Binds
    @Singleton
    abstract fun bindDashboardFeatureApi(
        impl: DashboardFeatureImpl
    ): DashboardFeatureApi
}
