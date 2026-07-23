package com.kharcha.tracker.domain.model

enum class SplitType {
    EQUAL,
    EXACT,
    PERCENTAGE
}

data class SplitGroup(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class SplitMember(
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val phone: String? = null
)

data class SplitExpense(
    val id: Long = 0,
    val groupId: Long,
    val description: String,
    val amount: Double,
    val paidByMemberId: Long,
    val splitType: SplitType,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

data class SplitExpenseShare(
    val id: Long = 0,
    val expenseId: Long,
    val memberId: Long,
    val shareAmount: Double
)

// Helper model for UI
data class MemberBalance(
    val memberId: Long,
    val memberName: String,
    val balance: Double // Positive means "is owed", negative means "owes"
)

data class GroupWithMembers(
    val group: SplitGroup,
    val members: List<SplitMember>,
    val totalExpense: Double = 0.0
)
