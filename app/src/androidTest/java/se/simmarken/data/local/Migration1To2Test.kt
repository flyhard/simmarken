package se.simmarken.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {
    private lateinit var context: Context
    private val testDb = "migration-test"

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(testDb)
    }

    @Test
    fun migration_backfillsStableId() {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(testDb)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS kids (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                name TEXT NOT NULL,
                                avatarColorArgb INTEGER NOT NULL,
                                createdAtEpochMillis INTEGER NOT NULL,
                                sortOrder INTEGER NOT NULL
                            )
                            """.trimIndent(),
                        )
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS requirement_progress (
                                kidId INTEGER NOT NULL,
                                requirementId INTEGER NOT NULL,
                                isAchieved INTEGER NOT NULL,
                                achievedAtEpochMillis INTEGER,
                                PRIMARY KEY(kidId, requirementId)
                            )
                            """.trimIndent(),
                        )
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS badge_progress (
                                kidId INTEGER NOT NULL,
                                badgeId INTEGER NOT NULL,
                                isGotten INTEGER NOT NULL,
                                achievedAtEpochMillis INTEGER,
                                gottenAtEpochMillis INTEGER,
                                PRIMARY KEY(kidId, badgeId)
                            )
                            """.trimIndent(),
                        )
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                },
            )
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase
        db.execSQL(
            """
            INSERT INTO kids (name, avatarColorArgb, createdAtEpochMillis, sortOrder)
            VALUES ('Ella', ${0xFF2196F3.toInt()}, 1000, 0)
            """.trimIndent(),
        )

        MIGRATION_1_2.migrate(db)

        db.query("SELECT stableId FROM kids").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.getString(0).isNotBlank())
        }

        db.close()
    }
}
