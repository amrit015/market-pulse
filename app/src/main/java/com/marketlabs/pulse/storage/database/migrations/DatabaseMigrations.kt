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

    // Migration from Version 3 to Version 4 for Market Indicators (Traffic Light)
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

    // Migration from Version 4 to Version 5 for Dashboard
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_state` (
                    `id` INTEGER NOT NULL, 
                    `isEquityOpen` INTEGER, 
                    `isFuturesOpen` INTEGER, 
                    `lastUpdated` INTEGER, 
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `dashboard_assets` (
                    `symbol` TEXT NOT NULL, 
                    `name` TEXT,
                    `description` TEXT,
                    `type` TEXT, 
                    `isInverted` INTEGER, 
                    `price` REAL, 
                    `previousClose` REAL, 
                    `changePercent` REAL, 
                    `rsi` REAL, 
                    `rsiStatus` TEXT, 
                    `macdSignal` TEXT, 
                    `technicalStatus` TEXT, 
                    `aiVerdict` TEXT, 
                    `lastUpdated` INTEGER, 
                    `sma20` REAL, 
                    `sma50` REAL, 
                    `sma200` REAL,
                    PRIMARY KEY(`symbol`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 5 to Version 6 (The Three Pillar Architecture)
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_indicators_new` (
                    `dateId` TEXT NOT NULL, 
                    `lastSyncedTimestamp` INTEGER NOT NULL, 
                    `marketPhase` TEXT, 
                    `macroVitals` TEXT, 
                    `marketAction` TEXT, 
                    PRIMARY KEY(`dateId`)
                )
                """.trimIndent()
            )
            db.execSQL("DROP TABLE IF EXISTS `market_indicators`")
            db.execSQL("ALTER TABLE `market_indicators_new` RENAME TO `market_indicators`")
        }
    }

    // Migration from Version 6 to Version 7 (Macro Dashboard Update)
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `market_state` ADD COLUMN `technicalSummary` TEXT")
            db.execSQL("ALTER TABLE `market_state` ADD COLUMN `technicalSummaryTimestamp` INTEGER")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `dashboard_assets_new` (
                    `symbol` TEXT NOT NULL, `name` TEXT, `type` TEXT, `description` TEXT, 
                    `isInverted` INTEGER, `price` REAL, `previousClose` REAL, `changePercent` REAL, 
                    `rsi` REAL, `rsiStatus` TEXT, `macdSignal` TEXT, `technicalStatus` TEXT, 
                    `lastUpdated` INTEGER, `sma20` REAL, `sma50` REAL, `sma200` REAL, 
                    PRIMARY KEY(`symbol`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                INSERT INTO `dashboard_assets_new` 
                (`symbol`, `name`, `type`, `description`, `isInverted`, `price`, `previousClose`, `changePercent`, `rsi`, `rsiStatus`, `macdSignal`, `technicalStatus`, `lastUpdated`, `sma20`, `sma50`, `sma200`) 
                SELECT `symbol`, `name`, `type`, `description`, `isInverted`, `price`, `previousClose`, `changePercent`, `rsi`, `rsiStatus`, `macdSignal`, `technicalStatus`, `lastUpdated`, `sma20`, `sma50`, `sma200` 
                FROM `dashboard_assets`
                """.trimIndent()
            )

            db.execSQL("DROP TABLE `dashboard_assets`")
            db.execSQL("ALTER TABLE `dashboard_assets_new` RENAME TO `dashboard_assets`")
        }
    }

    // Migration from Version 7 to Version 8 for Market Tail Risks
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_tail_risks` (
                    `date` TEXT NOT NULL, 
                    `lastSyncedTimestamp` INTEGER NOT NULL, 
                    `lastUpdated` INTEGER, 
                    `summary` TEXT, 
                    `risks` TEXT, 
                    `sourceNarrative` TEXT, 
                    PRIMARY KEY(`date`)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `weekly_playbook` (
                    `id` TEXT NOT NULL, 
                    `lastSyncedTimestamp` INTEGER, 
                    `lastUpdated` INTEGER, 
                    `weekStarting` TEXT, 
                    `events` TEXT, 
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 9 to Version 10 for the "Four Pillars + AI" Monolithic Engine
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create the new schema supporting all 5 data pillars
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_indicators_new` (
                    `dateId` TEXT NOT NULL, 
                    `lastSyncedTimestamp` INTEGER NOT NULL, 
                    `aiSynthesis` TEXT, 
                    `tacticalMomentum` TEXT, 
                    `systemicRisk` TEXT,
                    `valuation` TEXT,
                    `macroVitals` TEXT, 
                    PRIMARY KEY(`dateId`)
                )
                """.trimIndent()
            )

            // 2. We preserve the IDs and timestamps, and transfer macroVitals which is the only surviving schema.
            // The others (marketPhase, marketAction) are dropped and will be re-fetched automatically on next launch.
            db.execSQL(
                """
                INSERT INTO `market_indicators_new` (`dateId`, `lastSyncedTimestamp`, `macroVitals`)
                SELECT `dateId`, `lastSyncedTimestamp`, `macroVitals` FROM `market_indicators`
                """.trimIndent()
            )

            // 3. Swap the tables
            db.execSQL("DROP TABLE `market_indicators`")
            db.execSQL("ALTER TABLE `market_indicators_new` RENAME TO `market_indicators`")
        }
    }

    // 💡 NEW: Migration from Version 10 to 11 for Institutional Market Posture
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_posture` (
                    `id` TEXT NOT NULL, 
                    `naaimValue` REAL, 
                    `naaimStatus` TEXT, 
                    `naaimDescription` TEXT, 
                    `dixValue` REAL, 
                    `dixDate` TEXT, 
                    `dixStatus` TEXT, 
                    `dixDescription` TEXT, 
                    `netLiqValue` REAL, 
                    `netLiqStatus` TEXT,
                    `netLiqAssetsT` REAL, 
                    `netLiqTgaT` REAL, 
                    `netLiqRrpT` REAL, 
                    `netLiqDate` TEXT, 
                    `netLiqDescription` TEXT, 
                    `timestamp` INTEGER, 
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        MIGRATION_9_10, MIGRATION_10_11 // 💡 Added to registry
    )
}