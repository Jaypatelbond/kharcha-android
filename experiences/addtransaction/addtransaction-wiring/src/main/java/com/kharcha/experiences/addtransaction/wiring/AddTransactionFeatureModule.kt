package com.kharcha.experiences.addtransaction.wiring

import com.kharcha.experiences.addtransaction.api.AddTransactionFeatureApi
import com.kharcha.experiences.addtransaction.impl.AddTransactionFeatureImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddTransactionFeatureModule {

    @Binds
    @Singleton
    abstract fun bindAddTransactionFeatureApi(
        impl: AddTransactionFeatureImpl
    ): AddTransactionFeatureApi
}
