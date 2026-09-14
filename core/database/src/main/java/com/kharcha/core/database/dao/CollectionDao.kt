package com.kharcha.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kharcha.core.database.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY isDefault DESC, name ASC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE name = :name LIMIT 1")
    suspend fun getCollectionByName(name: String): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Query("UPDATE collections SET name = :newName WHERE name = :oldName")
    suspend fun updateCollectionName(oldName: String, newName: String)

    @Query("DELETE FROM collections WHERE name = :name")
    suspend fun deleteCollectionByName(name: String)
}
