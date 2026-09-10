package com.kharcha.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kharcha.core.database.entity.SplitExpenseEntity
import com.kharcha.core.database.entity.SplitExpenseShareEntity
import com.kharcha.core.database.entity.SplitGroupEntity
import com.kharcha.core.database.entity.SplitMemberEntity
import kotlinx.coroutines.flow.Flow

data class GroupTotal(
    val groupId: Long,
    val groupName: String,
    val totalExpense: Double
)

@Dao
interface SplitDao {

    // --- Groups ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SplitGroupEntity): Long

    @Query("SELECT * FROM split_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<SplitGroupEntity>>

    @Query("SELECT * FROM split_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): SplitGroupEntity?

    @Query("""
        SELECT g.id as groupId, g.name as groupName, COALESCE(SUM(e.amount), 0.0) as totalExpense 
        FROM split_groups g 
        LEFT JOIN split_expenses e ON g.id = e.groupId 
        GROUP BY g.id, g.name 
        ORDER BY g.createdAt DESC
    """)
    fun getGroupsWithTotalExpense(): Flow<List<GroupTotal>>

    // --- Members ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: SplitMemberEntity): Long

    @Query("SELECT * FROM split_members WHERE groupId = :groupId")
    fun getMembersByGroup(groupId: Long): Flow<List<SplitMemberEntity>>

    @Query("SELECT * FROM split_members WHERE id = :memberId")
    suspend fun getMemberById(memberId: Long): SplitMemberEntity?

    // --- Expenses ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: SplitExpenseEntity): Long

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY date DESC")
    fun getExpensesByGroup(groupId: Long): Flow<List<SplitExpenseEntity>>

    // --- Shares ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: SplitExpenseShareEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShares(shares: List<SplitExpenseShareEntity>)

    @Query("SELECT * FROM split_expense_shares WHERE expenseId = :expenseId")
    suspend fun getSharesForExpense(expenseId: Long): List<SplitExpenseShareEntity>
    
    @Query("SELECT * FROM split_expense_shares WHERE memberId = :memberId")
    suspend fun getSharesForMember(memberId: Long): List<SplitExpenseShareEntity>
    
    @Query("SELECT s.* FROM split_expense_shares s INNER JOIN split_expenses e ON s.expenseId = e.id WHERE e.groupId = :groupId")
    suspend fun getSharesByGroupId(groupId: Long): List<SplitExpenseShareEntity>
    
    @Query("SELECT * FROM split_members WHERE groupId = :groupId")
    suspend fun getMembersForGroupSync(groupId: Long): List<SplitMemberEntity>

    // --- UUID Backfill ---
    @Query("SELECT * FROM split_groups WHERE uuid = ''")
    suspend fun getGroupsWithEmptyUuid(): List<SplitGroupEntity>

    @androidx.room.Update
    suspend fun updateGroup(group: SplitGroupEntity)

    @Query("SELECT * FROM split_members WHERE uuid = ''")
    suspend fun getMembersWithEmptyUuid(): List<SplitMemberEntity>

    @androidx.room.Update
    suspend fun updateMember(member: SplitMemberEntity)

    @Query("SELECT * FROM split_expenses WHERE uuid = ''")
    suspend fun getExpensesWithEmptyUuid(): List<SplitExpenseEntity>

    @androidx.room.Update
    suspend fun updateExpense(expense: SplitExpenseEntity)

    // --- Deletion Queries ---
    @Query("DELETE FROM split_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)

    @Query("DELETE FROM split_expenses WHERE groupId = :groupId")
    suspend fun deleteExpensesByGroupId(groupId: Long)

    @Query("DELETE FROM split_expense_shares WHERE expenseId IN (SELECT id FROM split_expenses WHERE groupId = :groupId)")
    suspend fun deleteSharesByGroupId(groupId: Long)

    @Query("DELETE FROM split_members WHERE groupId = :groupId")
    suspend fun deleteMembersByGroupId(groupId: Long)

    @Query("DELETE FROM split_members WHERE id = :memberId")
    suspend fun deleteMemberById(memberId: Long)

    @Query("DELETE FROM split_expense_shares WHERE memberId = :memberId")
    suspend fun deleteSharesByMemberId(memberId: Long)

    @Query("DELETE FROM split_expense_shares WHERE expenseId IN (SELECT id FROM split_expenses WHERE paidByMemberId = :memberId)")
    suspend fun deleteSharesForExpensesPaidByMember(memberId: Long)

    @Query("DELETE FROM split_expenses WHERE paidByMemberId = :memberId")
    suspend fun deleteExpensesByMemberId(memberId: Long)

    @androidx.room.Transaction
    suspend fun deleteGroupCascading(groupId: Long) {
        deleteSharesByGroupId(groupId)
        deleteExpensesByGroupId(groupId)
        deleteMembersByGroupId(groupId)
        deleteGroupById(groupId)
    }

    @androidx.room.Transaction
    suspend fun removeMemberCascading(memberId: Long) {
        deleteSharesByMemberId(memberId)
        deleteSharesForExpensesPaidByMember(memberId)
        deleteExpensesByMemberId(memberId)
        deleteMemberById(memberId)
    }
}
