package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "badges",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index(value = ["categoryId", "code"], unique = true),
    ],
)
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val imageAssetPath: String?,
    val sortOrder: Int,
)
