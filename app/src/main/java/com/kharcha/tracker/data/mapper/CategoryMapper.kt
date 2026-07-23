package com.kharcha.tracker.data.mapper

import com.kharcha.tracker.data.local.entity.CategoryEntity
import com.kharcha.tracker.data.local.util.CategoryDefaults
import com.kharcha.tracker.domain.model.Category

private val defaultCategoriesMap = CategoryDefaults.getPrepopulatedCategories().associateBy { it.name }

fun getFallbackCategory(name: String, type: String, entityId: Int = 0): Category {
    val defaultCat = defaultCategoriesMap[name]
    return Category(
        id = entityId,
        name = name,
        type = type,
        iconName = defaultCat?.iconName ?: "more_horiz",
        color = defaultCat?.color ?: 0xFF808080.toInt(),
        isDefault = defaultCat?.isDefault ?: false
    )
}

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        type = type,
        iconName = iconName,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type,
        iconName = iconName,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived
    )
}
