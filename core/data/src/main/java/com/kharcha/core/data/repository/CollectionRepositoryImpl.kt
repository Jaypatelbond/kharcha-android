package com.kharcha.core.data.repository

import com.kharcha.core.database.dao.CollectionDao
import com.kharcha.core.database.dao.TransactionDao
import com.kharcha.core.database.entity.CollectionEntity
import com.kharcha.core.domain.repository.CollectionRepository
import com.kharcha.core.model.CollectionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao,
    private val transactionDao: TransactionDao
) : CollectionRepository {

    override fun getCollections(): Flow<List<CollectionModel>> {
        return collectionDao.getAllCollections().map { list ->
            list.map { entity ->
                CollectionModel(
                    id = entity.id,
                    name = entity.name,
                    isDefault = entity.isDefault,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun addCollection(name: String): Result<Unit> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotBlank()) { "Collection name cannot be blank" }
        val existing = collectionDao.getCollectionByName(trimmed)
        require(existing == null) { "Collection '$trimmed' already exists" }
        collectionDao.insertCollection(
            CollectionEntity(
                name = trimmed,
                isDefault = trimmed.equals("Home Expenses", ignoreCase = true)
            )
        )
    }

    override suspend fun renameCollection(oldName: String, newName: String): Result<Unit> = runCatching {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        require(trimmedNew.isNotBlank()) { "Collection name cannot be blank" }
        require(!trimmedOld.equals("Home Expenses", ignoreCase = true)) { "Cannot rename default 'Home Expenses' collection" }
        val existing = collectionDao.getCollectionByName(trimmedNew)
        require(existing == null) { "Collection '$trimmedNew' already exists" }

        collectionDao.updateCollectionName(trimmedOld, trimmedNew)
        transactionDao.updateCollectionName(trimmedOld, trimmedNew)
    }

    override suspend fun deleteCollection(name: String): Result<Unit> = runCatching {
        val trimmed = name.trim()
        require(!trimmed.equals("Home Expenses", ignoreCase = true)) { "Cannot delete default 'Home Expenses' collection" }

        collectionDao.deleteCollectionByName(trimmed)
        transactionDao.reassignTransactionsToDefaultCollection(trimmed)
    }
}
