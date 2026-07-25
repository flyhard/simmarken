package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kids",
    indices = [Index(value = ["stableId"], unique = true)],
)
data class KidEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stableId: String,
    val name: String,
    val avatarColorArgb: Int,
    val createdAtEpochMillis: Long,
    val sortOrder: Int = 0,
)
