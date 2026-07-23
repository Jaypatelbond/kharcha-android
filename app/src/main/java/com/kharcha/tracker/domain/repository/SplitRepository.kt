package com.kharcha.tracker.domain.repository

import com.kharcha.tracker.domain.model.GroupWithMembers
import com.kharcha.tracker.domain.model.MemberBalance
import com.kharcha.tracker.domain.model.SplitExpense
import com.kharcha.tracker.domain.model.SplitExpenseShare
import com.kharcha.tracker.domain.model.SplitGroup
import kotlinx.coroutines.flow.Flow

interface SplitRepository {

    suspend fun createGroup(name: String): Long
    
    fun getAllGroups(): Flow<List<SplitGroup>>
    
    fun getGroupList(): Flow<List<GroupWithMembers>>

    fun getGroupDetails(groupId: Long): Flow<GroupWithMembers>

    suspend fun addMember(groupId: Long, name: String, phone: String? = null): Long

    suspend fun addExpense(
        groupId: Long,
        description: String,
        amount: Double,
        paidByMemberId: Long,
        splitType: String,
        shares: List<SplitExpenseShare>
    ): Long

    fun getGroupExpenses(groupId: Long): Flow<List<SplitExpense>>

    suspend fun getGroupBalances(groupId: Long): List<MemberBalance>

    suspend fun ensureUuids()

    suspend fun exportGroup(groupId: Long): String
    suspend fun importGroup(json: String): Boolean
}
