package com.kharcha.tracker.data.repository

import com.kharcha.tracker.data.local.dao.SplitDao
import com.kharcha.tracker.data.local.entity.SplitExpenseEntity
import com.kharcha.tracker.data.local.entity.SplitExpenseShareEntity
import com.kharcha.tracker.data.local.entity.SplitGroupEntity
import com.kharcha.tracker.data.local.entity.SplitMemberEntity
import com.kharcha.tracker.domain.model.GroupWithMembers
import com.kharcha.tracker.domain.model.MemberBalance
import com.kharcha.tracker.domain.model.SplitExpense
import com.kharcha.tracker.domain.model.SplitExpenseShare
import com.kharcha.tracker.domain.model.SplitGroup
import com.kharcha.tracker.domain.model.SplitMember
import com.kharcha.tracker.domain.model.SplitType
import com.kharcha.tracker.domain.repository.SplitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SplitRepositoryImpl @Inject constructor(
    private val dao: SplitDao
) : SplitRepository {

    override suspend fun createGroup(name: String): Long {
        return dao.insertGroup(SplitGroupEntity(name = name))
    }

    override fun getAllGroups(): Flow<List<SplitGroup>> {
        return dao.getAllGroups().map { entities ->
            entities.map { entity ->
                SplitGroup(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override fun getGroupList(): Flow<List<GroupWithMembers>> {
        return dao.getGroupsWithTotalExpense().map { groupTotals ->
            groupTotals.map { groupTotal ->
                val group = SplitGroup(
                    id = groupTotal.groupId,
                    name = groupTotal.groupName
                )
                // Fetch members for this group (suspend call inside map is tricky in Flow map, but we can usage map with async if needed, 
                // but standard List map is blocking. Since this is IO dispatcher context usually, it might be okay for small lists.
                // Better approach: Use a different query or relation. 
                // For now, doing it effectively synchronously for each item.
                val members = dao.getMembersForGroupSync(groupTotal.groupId).map { entity ->
                    SplitMember(
                        id = entity.id,
                        groupId = entity.groupId,
                        name = entity.name,
                        phone = entity.phone
                    )
                }

                GroupWithMembers(
                    group = group,
                    members = members,
                    totalExpense = groupTotal.totalExpense
                )
            }
        }
    }

    override fun getGroupDetails(groupId: Long): Flow<GroupWithMembers> {
        val groupFlow = dao.getGroupsWithTotalExpense().map { list ->
            list.find { it.groupId == groupId }
        }
        val membersFlow = dao.getMembersByGroup(groupId)

        return combine(groupFlow, membersFlow) { groupInfo, members ->
            // If groupInfo is null, it means group doesn't exist or hasn't loaded. Handle gracefully.
            // But we can fetch GroupEntity directly if needed. Using getGroupsWithTotalExpense is efficient for total.
            // Let's assume group exists if we are here.
            val group = if (groupInfo != null) {
                SplitGroup(id = groupInfo.groupId, name = groupInfo.groupName)
            } else {
                val entity = dao.getGroupById(groupId) ?: return@combine GroupWithMembers(SplitGroup(0, ""), emptyList())
                SplitGroup(id = entity.id, name = entity.name, createdAt = entity.createdAt)
            }
            
            val totalExpense = groupInfo?.totalExpense ?: 0.0

            val domainMembers = members.map { entity ->
                SplitMember(
                    id = entity.id,
                    groupId = entity.groupId,
                    name = entity.name,
                    phone = entity.phone
                )
            }

            GroupWithMembers(
                group = group,
                members = domainMembers,
                totalExpense = totalExpense
            )
        }
    }

    override suspend fun addMember(groupId: Long, name: String, phone: String?): Long {
        return dao.insertMember(
            SplitMemberEntity(
                groupId = groupId,
                name = name,
                phone = phone
            )
        )
    }

    override suspend fun addExpense(
        groupId: Long,
        description: String,
        amount: Double,
        paidByMemberId: Long,
        splitType: String,
        shares: List<SplitExpenseShare>
    ): Long {
        val expenseId = dao.insertExpense(
            SplitExpenseEntity(
                groupId = groupId,
                description = description,
                amount = amount,
                paidByMemberId = paidByMemberId,
                splitType = splitType
            )
        )

        val shareEntities = shares.map { share ->
            SplitExpenseShareEntity(
                expenseId = expenseId,
                memberId = share.memberId,
                shareAmount = share.shareAmount
            )
        }
        dao.insertShares(shareEntities)
        return expenseId
    }

    override fun getGroupExpenses(groupId: Long): Flow<List<SplitExpense>> {
        return dao.getExpensesByGroup(groupId).map { entities ->
            entities.map { entity ->
                SplitExpense(
                    id = entity.id,
                    groupId = entity.groupId,
                    description = entity.description,
                    amount = entity.amount,
                    paidByMemberId = entity.paidByMemberId,
                    splitType = SplitType.valueOf(entity.splitType),
                    date = entity.date,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    override suspend fun getGroupBalances(groupId: Long): List<MemberBalance> {
        val members = dao.getMembersByGroup(groupId).first() // Use first() to get current list
        val expenses = dao.getExpensesByGroup(groupId).first() // Get expenses
        val allShares = dao.getSharesByGroupId(groupId) // Get all shares for this group
        
        val balances = mutableMapOf<Long, Double>()
        members.forEach { balances[it.id] = 0.0 } // Initialize

        for (expense in expenses) {
            // Credit the payer
            balances[expense.paidByMemberId] = (balances[expense.paidByMemberId] ?: 0.0) + expense.amount
        }
        
        for (share in allShares) {
             // Debit the sharers
             balances[share.memberId] = (balances[share.memberId] ?: 0.0) - share.shareAmount
        }

        return balances.map { (memberId, balance) ->
            val memberName = members.find { it.id == memberId }?.name ?: "Unknown"
            MemberBalance(
                memberId = memberId,
                memberName = memberName,
                balance = balance
            )
        }
    }

    override suspend fun ensureUuids() {
        // Groups
        dao.getGroupsWithEmptyUuid().forEach { group ->
            dao.updateGroup(group.copy(uuid = java.util.UUID.randomUUID().toString()))
        }
        // Members
        dao.getMembersWithEmptyUuid().forEach { member ->
            dao.updateMember(member.copy(uuid = java.util.UUID.randomUUID().toString()))
        }
        // Expenses
        dao.getExpensesWithEmptyUuid().forEach { expense ->
            dao.updateExpense(expense.copy(uuid = java.util.UUID.randomUUID().toString()))
        }
    }

    override suspend fun exportGroup(groupId: Long): String {
        // 1. Fetch Group
        val groupEntity = dao.getGroupById(groupId) ?: throw Exception("Group not found")
        
        // 2. Fetch Members
        val memberEntities = dao.getMembersForGroupSync(groupId)
        
        // 3. Fetch Expenses & Shares
        val expenseEntities = dao.getExpensesByGroup(groupId).first()
        val shareEntities = dao.getSharesByGroupId(groupId)

        // 4. Map to DTOs
        val groupDto = com.kharcha.tracker.data.dto.GroupDto(
            uuid = groupEntity.uuid,
            name = groupEntity.name,
            createdAt = groupEntity.createdAt
        )

        val memberDtos = memberEntities.map { 
            com.kharcha.tracker.data.dto.MemberDto(
                uuid = it.uuid,
                name = it.name,
                phone = it.phone
            ) 
        }

        val expenseDtos = expenseEntities.map { expense ->
            val expenseShares = shareEntities.filter { it.expenseId == expense.id }
            val shareDtos = expenseShares.map { share ->
                val memberUuid = memberEntities.find { it.id == share.memberId }?.uuid ?: ""
                com.kharcha.tracker.data.dto.ShareDto(memberUuid = memberUuid, shareAmount = share.shareAmount)
            }
            val payerUuid = memberEntities.find { it.id == expense.paidByMemberId }?.uuid ?: ""
            
            com.kharcha.tracker.data.dto.ExpenseDto(
                uuid = expense.uuid,
                description = expense.description,
                amount = expense.amount,
                paidByMemberUuid = payerUuid,
                splitType = expense.splitType,
                date = expense.date,
                shares = shareDtos
            )
        }

        val syncDto = com.kharcha.tracker.data.dto.GroupSyncDto(
            group = groupDto,
            members = memberDtos,
            expenses = expenseDtos
        )

        return com.google.gson.Gson().toJson(syncDto)
    }

    override suspend fun importGroup(json: String): Boolean {
        try {
            val dto = com.google.gson.Gson().fromJson(json, com.kharcha.tracker.data.dto.GroupSyncDto::class.java)
            
            // 1. Sync Group
            // Check if group exists by UUID, if not insert. If yes, update? No, keep local ID.
            val existingGroup = dao.getAllGroups().first().find { it.uuid == dto.group.uuid }
            val groupId = if (existingGroup != null) {
                existingGroup.id
            } else {
                dao.insertGroup(SplitGroupEntity(
                    uuid = dto.group.uuid,
                    name = dto.group.name,
                    createdAt = dto.group.createdAt
                ))
            }

            // 2. Sync Members
            // Map UUID -> Local ID
            val memberUuidToIdMap = mutableMapOf<String, Long>()
            val existingMembers = dao.getMembersForGroupSync(groupId)
            
            dto.members.forEach { memberDto ->
                val existingMember = existingMembers.find { it.uuid == memberDto.uuid }
                if (existingMember != null) {
                    memberUuidToIdMap[memberDto.uuid] = existingMember.id
                } else {
                    val newId = dao.insertMember(SplitMemberEntity(
                        uuid = memberDto.uuid,
                        groupId = groupId,
                        name = memberDto.name,
                        phone = memberDto.phone
                    ))
                    memberUuidToIdMap[memberDto.uuid] = newId
                }
            }

            // 3. Sync Expenses
            val existingExpenses = dao.getExpensesByGroup(groupId).first()

            dto.expenses.forEach { expenseDto ->
                if (existingExpenses.none { it.uuid == expenseDto.uuid }) {
                    // Start Transaction ideally
                    val payerId = memberUuidToIdMap[expenseDto.paidByMemberUuid] ?: return@forEach // Skip if payer unknown (shouldn't happen)
                    
                    val newExpenseId = dao.insertExpense(SplitExpenseEntity(
                        uuid = expenseDto.uuid,
                        groupId = groupId,
                        description = expenseDto.description,
                        amount = expenseDto.amount,
                        paidByMemberId = payerId,
                        splitType = expenseDto.splitType,
                        date = expenseDto.date
                    ))

                    val newShares = expenseDto.shares.mapNotNull { shareDto ->
                        val memberId = memberUuidToIdMap[shareDto.memberUuid] ?: return@mapNotNull null
                        SplitExpenseShareEntity(
                            expenseId = newExpenseId,
                            memberId = memberId,
                            shareAmount = shareDto.shareAmount
                        )
                    }
                    if (newShares.isNotEmpty()) {
                        dao.insertShares(newShares)
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
