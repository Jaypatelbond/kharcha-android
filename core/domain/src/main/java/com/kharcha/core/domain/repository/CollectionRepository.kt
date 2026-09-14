package com.kharcha.core.domain.repository

import com.kharcha.core.model.CollectionModel
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getCollections(): Flow<List<CollectionModel>>
    suspend fun addCollection(name: String): Result<Unit>
    suspend fun renameCollection(oldName: String, newName: String): Result<Unit>
    suspend fun deleteCollection(name: String): Result<Unit>
}
