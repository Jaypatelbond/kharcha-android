package com.kharcha.tracker.domain.model

data class Category(
    val id: Int = 0,
    val name: String,
    val type: String, // INCOME or EXPENSE
    val iconName: String,
    val color: Int, // ARGB
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
) {
    val displayName: String
        get() = name
    
    val isIncome: Boolean
        get() = type == "INCOME"
}
