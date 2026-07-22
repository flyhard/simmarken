package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "requirements",
    foreignKeys = [
        ForeignKey(
            entity = BadgeEntity::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("badgeId"),
        Index(value = ["badgeId", "code"], unique = true),
    ],
)
data class RequirementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val badgeId: Long,
    val code: String,
    val textSv: String,
    val textEn: String,
    val sortOrder: Int,
)
