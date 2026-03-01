package com.marketlabs.pulse.storage.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Centralized registry for all database migrations
 * Add migrations here
 */
object DatabaseMigrations {

    // Migration from Version 1 to Version 2 for Market News
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
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

    // Migration from Version 2 to Version 3 for Risk Radar
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create the brand new 'market_risk' table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_risk` (
                    `date` TEXT NOT NULL, 
                    `lastSyncedTimestamp` INTEGER, 
                    `score` INTEGER, 
                    `previousScore` INTEGER, 
                    `trend` TEXT, 
                    `status` TEXT, 
                    `gauges` TEXT, 
                    PRIMARY KEY(`date`)
                )
                """.trimIndent()
            )
        }
    }

    // 💡 NEW: Migration from Version 3 to Version 4 for Market Indicators (Traffic Light)
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_indicators` (
                    `dateId` TEXT NOT NULL, 
                    `lastSyncedTimestamp` INTEGER NOT NULL, 
                    `summary` TEXT, 
                    `trendPhase` TEXT, 
                    `healthPhase` TEXT, 
                    `riskPhase` TEXT, 
                    PRIMARY KEY(`dateId`)
                )
                """.trimIndent()
            )
        }
    }

    // 💡 UPDATED: Add the new migration to the active list
    val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}