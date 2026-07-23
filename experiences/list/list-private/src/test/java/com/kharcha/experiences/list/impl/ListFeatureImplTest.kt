package com.kharcha.experiences.list.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class ListFeatureImplTest {

    @Test
    fun `getListFeatureName returns correct string`() {
        val listFeature = ListFeatureImpl()
        val result = listFeature.getListFeatureName()
        assertEquals("List Feature Private Implementation", result)
    }
}
