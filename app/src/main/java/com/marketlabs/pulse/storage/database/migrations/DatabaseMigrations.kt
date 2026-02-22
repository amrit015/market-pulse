package com.marketlabs.pulse.storage.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Centralized registry for all database migrations
 * Add migrations here
 */
object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // 1. Create the brand new 'market_news' table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_news` (
                    `id` TEXT NOT NULL, 
                    `lastSyncedTimestamp` INTEGER NOT NULL, 
                    `sourceCount` INTEGER, 
                    `stories` TEXT, 
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    // List of all active migrations to easily add to the Room builder
    val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
}