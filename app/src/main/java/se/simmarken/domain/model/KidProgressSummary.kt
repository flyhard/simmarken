package se.simmarken.domain.model

data class KidProgressSummary(
    val kidId: Long,
    val inProgressCount: Int = 0,
    val toBuyCount: Int = 0,
)
