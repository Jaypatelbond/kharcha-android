package com.kharcha.tracker.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetTest {

    @Test
    fun isOverallBudget_whenCategoryNameIsAll_returnsTrue() {
        val budget = Budget(
            id = 1,
            categoryName = "All",
            amount = 15000.0,
            month = "2026-06"
        )
        assertTrue(budget.isOverallBudget)
    }

    @Test
    fun isOverallBudget_whenCategoryNameIsSpecific_returnsFalse() {
        val budget = Budget(
            id = 2,
            categoryName = "Food & Groceries",
            amount = 3000.0,
            month = "2026-06"
        )
        assertFalse(budget.isOverallBudget)
    }
}
