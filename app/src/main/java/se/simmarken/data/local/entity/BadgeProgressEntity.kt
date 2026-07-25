package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "badge_progress",
    primaryKeys = ["kidId", "badgeId"],
    foreignKeys = [
        ForeignKey(
            entity = KidEntity::class,
            parentColumns = ["id"],
            childColumns = ["kidId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BadgeEntity::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("kidId"),
        Index("badgeId"),
    ],
)
data class BadgeProgressEntity(
    val kidId: Long,
    val badgeId: Long,
    val isGotten: Boolean = false,
    val achievedAtEpochMillis: Long? = null,
    val gottenAtEpochMillis: Long? = null,
    val updatedAtEpochMillis: Long = 0L,
)
