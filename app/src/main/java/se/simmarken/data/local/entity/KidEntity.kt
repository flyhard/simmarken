package se.simmarken.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kids")
data class KidEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarColorArgb: Int,
    val createdAtEpochMillis: Long,
    val sortOrder: Int = 0,
)
