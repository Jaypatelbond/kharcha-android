package com.kharcha.tracker.data.repository

import com.kharcha.tracker.data.local.dao.CategoryDao
import com.kharcha.tracker.data.mapper.toDomain
import com.kharcha.tracker.data.mapper.toEntity
import com.kharcha.tracker.domain.model.Category
import com.kharcha.tracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        dao.getAllCategories().map { entities -> entities.map { it.toDomain() } }

    override fun getCategoriesByType(type: String): Flow<List<Category>> =
        dao.getCategoriesByType(type).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertCategory(category: Category) {
        dao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        dao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        dao.deleteCategory(category.toEntity())
    }
    
    override suspend fun getCategoryByName(name: String, type: String): Category? {
        return dao.getCategoryByNameAndType(name, type)?.toDomain()
    }
}
