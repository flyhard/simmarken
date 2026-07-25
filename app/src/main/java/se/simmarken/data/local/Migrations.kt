package se.simmarken.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE kids ADD COLUMN stableId TEXT NOT NULL DEFAULT ''")

        db.query("SELECT id FROM kids").use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val stableId = UUID.randomUUID().toString()
                db.execSQL("UPDATE kids SET stableId = ? WHERE id = ?", arrayOf(stableId, id))
            }
        }

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_kids_stableId ON kids(stableId)")

        db.execSQL(
            "ALTER TABLE requirement_progress ADD COLUMN updatedAtEpochMillis INTEGER NOT NULL DEFAULT 0",
        )
        val now = System.currentTimeMillis()
        db.execSQL(
            """
            UPDATE requirement_progress
            SET updatedAtEpochMillis = COALESCE(
                achievedAtEpochMillis,
                (SELECT createdAtEpochMillis FROM kids WHERE kids.id = requirement_progress.kidId),
                $now
            )
            """.trimIndent(),
        )

        db.execSQL(
            "ALTER TABLE badge_progress ADD COLUMN updatedAtEpochMillis INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            """
            UPDATE badge_progress
            SET updatedAtEpochMillis = COALESCE(
                gottenAtEpochMillis,
                achievedAtEpochMillis,
                (SELECT createdAtEpochMillis FROM kids WHERE kids.id = badge_progress.kidId),
                $now
            )
            """.trimIndent(),
        )
    }
}
