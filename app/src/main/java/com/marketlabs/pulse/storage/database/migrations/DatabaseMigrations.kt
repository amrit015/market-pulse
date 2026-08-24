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

    // Migration from Version 11 to 12 for the Stock Analysis "Deep Study" domain
    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_stocks` (
                    `symbol` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `price` REAL,
                    `changePercent` REAL,
                    `technicalIndicators` TEXT,
                    `technicalSetup` TEXT,
                    `executiveThesis` TEXT,
                    `topNewsStream` TEXT,
                    `battlegroundLevels` TEXT,
                    `contextVault` TEXT,
                    `timestamp` INTEGER,
                    PRIMARY KEY(`symbol`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 12 to 13: the backend split the single deep-study document into a
    // small always-cached preview and a large detail fetched only on tap, so `market_stocks`
    // becomes two tables — `market_stock_previews` (was `market_stocks`) and the new
    // `market_stock_details`. No data-preserving copy: the old cache is just re-fetched.
    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `market_stocks`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_stock_previews` (
                    `symbol` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `name` TEXT,
                    `schemaVersion` INTEGER,
                    `analysisDate` TEXT,
                    `price` REAL,
                    `changePercent` REAL,
                    `plainRead` TEXT,
                    `technicalSetup` TEXT,
                    `setupNetBias` INTEGER,
                    `setupConfidence` INTEGER,
                    `conditionChips` TEXT,
                    `regimeAtAnalysis` TEXT,
                    `previousClose` REAL,
                    `previousSetup` TEXT,
                    `previousNetBias` INTEGER,
                    `setupChanged` INTEGER,
                    `chipsAdded` TEXT,
                    `chipsRemoved` TEXT,
                    `headlineMetrics` TEXT,
                    `hasDirectNews` INTEGER,
                    `topHeadline` TEXT,
                    `contentFlags` TEXT,
                    `detailVersion` INTEGER,
                    `timestamp` INTEGER,
                    PRIMARY KEY(`symbol`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_stock_details` (
                    `symbol` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `detailVersion` INTEGER,
                    `analysisDate` TEXT,
                    `technicalIndicators` TEXT,
                    `levels` TEXT,
                    `setupSignals` TEXT,
                    `setupConfirming` TEXT,
                    `setupConflicting` TEXT,
                    `conditionLabels` TEXT,
                    `watchList` TEXT,
                    `fundamentals` TEXT,
                    `macro` TEXT,
                    `technicalRead` TEXT,
                    `notCovered` TEXT,
                    `scenarios` TEXT,
                    `considerations` TEXT,
                    `executiveThesis` TEXT,
                    `topNewsStream` TEXT,
                    `contextVault` TEXT,
                    `calls` TEXT,
                    `timestamp` INTEGER,
                    PRIMARY KEY(`symbol`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 13 to 14: the 2026-08-17 backend revamp consolidated
    // market_pulse's verdict/signal/the_read split into one verdict object (dropping `call`,
    // renaming `action` to `posture`) and added drivers[], market_position, watch[], risks[]
    // as new sibling fields, while market_outlook is no longer modeled client-side at all.
    // verdict/leadStories/macroMix/dominoEffect are stored as JSON TEXT columns whose shape
    // changed, so a copied old row would carry stale/incompatible JSON for those columns --
    // no data-preserving copy, same as MIGRATION_12_13: the old cache is just re-fetched on
    // next sync.
    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `market_pulse`")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_pulse` (
                    `dateId` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `lastUpdated` INTEGER NOT NULL,
                    `reportType` TEXT NOT NULL,
                    `verdict` TEXT,
                    `drivers` TEXT,
                    `position` TEXT,
                    `leadStories` TEXT,
                    `macroMix` TEXT,
                    `dominoEffect` TEXT,
                    `watch` TEXT,
                    `risks` TEXT,
                    `whatChanged` TEXT,
                    PRIMARY KEY(`dateId`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 14 to 15: Horizon.riskLevel moved from a raw String to the
    // RiskImpactLevel enum (reusing the market_risk domain's severity vocabulary instead of a
    // second one) so it colors the same way RiskItem.severity does. Room's SQL schema for
    // `market_pulse` doesn't change -- `position` is still one JSON TEXT column -- but a row
    // cached by the previous version holds the raw backend string (e.g. "MODERATE") at
    // horizons.*.riskLevel, which isn't one of RiskImpactLevel's declared constant names
    // (EXTREME/HIGH/MEDIUM/LOW/UNKNOWN) and crashes Moshi's default enum adapter on read. Same
    // "no data-preserving copy, just force a re-fetch" fix as MIGRATION_12_13/13_14.
    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM `market_pulse`")
        }
    }

    // Migration from Version 15 to 16: market_pulse gained two new backend fields (2026-08-21) --
    // drivers[].data_direction (a sibling field on the existing `drivers` JSON column, no schema
    // change needed there) and the new top-level whats_new[] list, which gets its own column, same
    // pattern as watch/risks/drivers rather than nesting inside an existing blob. Unlike
    // MIGRATION_13_14/14_15, this is purely additive -- no existing column's JSON shape changed,
    // so a plain `ADD COLUMN` preserves the existing cache instead of forcing a re-fetch; old rows
    // just read back with `whatsNew` null until the next sync.
    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `market_pulse` ADD COLUMN `whatsNew` TEXT")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
        MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16 // 💡 Added to registry
    )
}