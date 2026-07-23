package com.kharcha.core.data.mapper

import com.kharcha.core.database.entity.CategoryEntity
import com.kharcha.core.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryMapperTest {

    @Test
    fun toDomainTest() {
        val entity = CategoryEntity(
            id = 12,
            name = "Rent & Bills",
            type = "EXPENSE",
            iconName = "home",
            color = 0xFF556677.toInt(),
            isDefault = false,
            isArchived = true,
            createdAt = 1000L
        )

        val domain = entity.toDomain()

        assertEquals(12, domain.id)
        assertEquals("Rent & Bills", domain.name)
        assertEquals("EXPENSE", domain.type)
        assertEquals("home", domain.iconName)
        assertEquals(0xFF556677.toInt(), domain.color)
        assertFalse(domain.isDefault)
        assertTrue(domain.isArchived)
    }

    @Test
    fun toEntityTest() {
        val domain = Category(
            id = 45,
            name = "Salary",
            type = "INCOME",
            iconName = "attach_money",
            color = 0xFF112233.toInt(),
            isDefault = true,
            isArchived = false
        )

        val entity = domain.toEntity()

        assertEquals(45, entity.id)
        assertEquals("Salary", entity.name)
        assertEquals("INCOME", entity.type)
        assertEquals("attach_money", entity.iconName)
        assertEquals(0xFF112233.toInt(), entity.color)
        assertTrue(entity.isDefault)
        assertFalse(entity.isArchived)
    }
}
