package com.kharcha.core.model

data class Budget(
    val id: Int = 0,
    val categoryName: String, // "All" represents overall budget, or specific category name
    val amount: Double,
    val month: String, // Format: "YYYY-MM"
    val createdAt: Long = System.currentTimeMillis()
) {
    val isOverallBudget: Boolean
        get() = categoryName == "All"
}
