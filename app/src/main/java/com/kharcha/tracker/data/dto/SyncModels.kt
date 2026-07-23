package com.kharcha.tracker.data.dto

data class GroupSyncDto(
    val group: GroupDto,
    val members: List<MemberDto>,
    val expenses: List<ExpenseDto>
)

data class GroupDto(
    val uuid: String,
    val name: String,
    val createdAt: Long
)

data class MemberDto(
    val uuid: String,
    val name: String,
    val phone: String?
)

data class ExpenseDto(
    val uuid: String,
    val description: String,
    val amount: Double,
    val paidByMemberUuid: String, // Reference by UUID
    val splitType: String,
    val date: Long,
    val shares: List<ShareDto>
)

data class ShareDto(
    val memberUuid: String, // Reference by UUID
    val shareAmount: Double
)
