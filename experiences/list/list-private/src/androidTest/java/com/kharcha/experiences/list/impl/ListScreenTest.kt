package com.kharcha.experiences.list.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import com.kharcha.experiences.list.impl.ui.ListScreen
import org.junit.Rule
import org.junit.Test

class ListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun listScreen_displaysText() {
        composeTestRule.setContent {
            ListScreen()
        }

        composeTestRule
            .onNodeWithTag("ListScreenText")
            .assertIsDisplayed()
    }
}
