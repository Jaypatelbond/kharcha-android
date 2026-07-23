package com.kharcha.experiences.list.impl

import com.kharcha.experiences.list.api.ListFeatureApi
import javax.inject.Inject

class ListFeatureImpl @Inject constructor() : ListFeatureApi {
    override fun getListFeatureName(): String {
        return "List Feature Private Implementation"
    }
}
