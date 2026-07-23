package com.kharcha.core.domain.repository

import com.kharcha.core.model.GroupWithMembers
import com.kharcha.core.model.MemberBalance
import com.kharcha.core.model.SplitExpense
import com.kharcha.core.model.SplitExpenseShare
import com.kharcha.core.model.SplitGroup
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
