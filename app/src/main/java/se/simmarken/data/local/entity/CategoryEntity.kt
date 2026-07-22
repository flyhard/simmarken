package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CatalogEntity::class,
            parentColumns = ["id"],
            childColumns = ["catalogId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("catalogId"),
        Index(value = ["catalogId", "code"], unique = true),
    ],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: Long,
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val sortOrder: Int,
)
