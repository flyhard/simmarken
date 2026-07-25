package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "requirement_progress",
    primaryKeys = ["kidId", "requirementId"],
    foreignKeys = [
        ForeignKey(
            entity = KidEntity::class,
            parentColumns = ["id"],
            childColumns = ["kidId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RequirementEntity::class,
            parentColumns = ["id"],
            childColumns = ["requirementId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("kidId"),
        Index("requirementId"),
    ],
)
data class RequirementProgressEntity(
    val kidId: Long,
    val requirementId: Long,
    val isAchieved: Boolean,
    val achievedAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long = 0L,
)
