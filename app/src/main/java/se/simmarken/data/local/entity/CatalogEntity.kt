package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "catalogs",
    indices = [Index(value = ["code"], unique = true)],
)
data class CatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val catalogVersion: String,
    val sortOrder: Int = 0,
)
