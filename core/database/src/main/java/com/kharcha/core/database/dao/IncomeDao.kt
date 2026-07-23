package com.kharcha.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kharcha.core.database.entity.IncomeProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income_profiles ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatest(): Flow<IncomeProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeProfileEntity)

    @Update
    suspend fun update(income: IncomeProfileEntity)

    @Query("DELETE FROM income_profiles")
    suspend fun deleteAll()
}
