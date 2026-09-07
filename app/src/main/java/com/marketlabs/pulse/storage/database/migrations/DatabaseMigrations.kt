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

    // Migration from Version 16 to 17: new `market_charts` table backing period charts
    // (5D/1M/6M/YTD/1Y), one row per `(symbol, rangeKey)` pair rather than one row per symbol --
    // switching the range picker is a different cached row, not a client-side slice of one full
    // series (the backend's `?days=N` isn't a literal point-count trim, so only the backend's own
    // per-range filtering is trustworthy -- see `ChartModels.kt`'s doc comment). Purely additive,
    // no existing table touched.
    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_charts` (
                    `symbol` TEXT NOT NULL,
                    `rangeKey` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `name` TEXT,
                    `type` TEXT,
                    `points` TEXT,
                    PRIMARY KEY(`symbol`, `rangeKey`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 17 to 18: new `metric_history` table backing the indicator
    // detail page's history chart -- one row per `metricId` (no range key, unlike
    // `market_charts`: the spec explicitly says not to build a range picker for this yet, so
    // each metric only ever has one cached series). Purely additive, no existing table touched.
    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `metric_history` (
                    `metricId` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `points` TEXT,
                    PRIMARY KEY(`metricId`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 18 to 19: Posture/Positioning revamp (2026-08-26) -- Posture's three
    // existing gauges (naaim_exposure, dark_pool_index, net_liquidity) each gain a
    // last_observation/delta/delta_direction/fetched_at/stale_since envelope, and the document
    // gains a new synthesis narrative block. Kept as flat nullable columns, same shape this entity
    // already used for every existing field, rather than restructuring onto JSON-blob columns like
    // Indicators/Stocks -- purely additive `ADD COLUMN`s, same style as MIGRATION_15_16, no
    // existing column touched. The new sibling `market_positioning` table (greenfield domain) is
    // created here too since both land in the same revamp.
    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `naaimLastObsValue` REAL")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `naaimLastObsStatus` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `naaimLastObsObservedAt` INTEGER")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `naaimDelta` REAL")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `naaimDeltaDirection` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `naaimFetchedAt` INTEGER")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `naaimStaleSince` INTEGER")

            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `dixLastObsValue` REAL")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `dixLastObsStatus` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `dixLastObsObservedAt` INTEGER")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `dixDelta` REAL")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `dixDeltaDirection` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `dixFetchedAt` INTEGER")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `dixStaleSince` INTEGER")

            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `netLiqLastObsValue` REAL")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `netLiqLastObsStatus` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `netLiqLastObsObservedAt` INTEGER")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `netLiqDelta` REAL")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `netLiqDeltaDirection` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `netLiqFetchedAt` INTEGER")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `netLiqStaleSince` INTEGER")

            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `synthesisHeadline` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `synthesisDetail` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `synthesisGeneratedAt` INTEGER")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `synthesisContentFlags` TEXT")
            db.execSQL("ALTER TABLE `market_posture` ADD COLUMN `synthesisState` TEXT")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_positioning` (
                    `id` TEXT NOT NULL,
                    `retailSentiment` TEXT,
                    `institutionalPositioning` TEXT,
                    `shortInterest` TEXT,
                    `synthesisHeadline` TEXT,
                    `synthesisDetail` TEXT,
                    `synthesisGeneratedAt` INTEGER,
                    `synthesisContentFlags` TEXT,
                    `synthesisState` TEXT,
                    `timestamp` INTEGER,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 19 to 20: Risks/Events revision (2026-08-29) -- both
    // market_insights/current_risks and market_insights/weekly_playbook gain the same synthesis
    // narrative block Posture/Positioning already carry, stored as the same 5 flat nullable
    // columns MIGRATION_18_19 added to `market_posture` (synthesisHeadline/Detail/GeneratedAt/
    // ContentFlags/State). `weekly_playbook` gets a purely additive `ADD COLUMN` set, same style
    // as MIGRATION_18_19. `market_tail_risks` also drops its `summary` column (backend
    // hard-deleted the field; its content is now synthesis.detail) -- SQLite's `ADD COLUMN` can't
    // drop a column, so this rebuilds the table instead, preserving every row's remaining columns
    // (same recreate-with-copy shape as MIGRATION_6_7), rather than force a re-fetch the way
    // MIGRATION_12_13/13_14/14_15 do for their incompatible-JSON-shape cases -- this table's
    // surviving columns didn't change shape, only one column is gone.
    val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_tail_risks_new` (
                    `date` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `lastUpdated` INTEGER,
                    `risks` TEXT,
                    `sourceNarrative` TEXT,
                    `synthesisHeadline` TEXT,
                    `synthesisDetail` TEXT,
                    `synthesisGeneratedAt` INTEGER,
                    `synthesisContentFlags` TEXT,
                    `synthesisState` TEXT,
                    PRIMARY KEY(`date`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `market_tail_risks_new`
                (`date`, `lastSyncedTimestamp`, `lastUpdated`, `risks`, `sourceNarrative`)
                SELECT `date`, `lastSyncedTimestamp`, `lastUpdated`, `risks`, `sourceNarrative`
                FROM `market_tail_risks`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `market_tail_risks`")
            db.execSQL("ALTER TABLE `market_tail_risks_new` RENAME TO `market_tail_risks`")

            db.execSQL("ALTER TABLE `weekly_playbook` ADD COLUMN `synthesisHeadline` TEXT")
            db.execSQL("ALTER TABLE `weekly_playbook` ADD COLUMN `synthesisDetail` TEXT")
            db.execSQL("ALTER TABLE `weekly_playbook` ADD COLUMN `synthesisGeneratedAt` INTEGER")
            db.execSQL("ALTER TABLE `weekly_playbook` ADD COLUMN `synthesisContentFlags` TEXT")
            db.execSQL("ALTER TABLE `weekly_playbook` ADD COLUMN `synthesisState` TEXT")
        }
    }

    // Migration from Version 20 to 21: market_pulse gains the new market_sentiment field
    // (spec-20260902-market-sentiment-android.md) -- an AI-authored cohort-positioning synthesis
    // riding inside the existing pulse response. Purely additive `ADD COLUMN`, same style as
    // MIGRATION_15_16/18_19 -- no existing column touched, old rows just read back with
    // marketSentiment null until the next sync.
    val MIGRATION_20_21 = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `market_pulse` ADD COLUMN `marketSentiment` TEXT")
        }
    }

    // Migration from Version 21 to 22: per-symbol intelligence (short interest, Daily Digest,
    // Deep Dive) -- market_stock_previews gains 5 new columns (digest headline + 4 deep-dive
    // date/version fields), market_stock_details gains 1 (digest sections), and a brand new
    // market_stock_deep_dives table is created for the separately-fetched Deep Dive subdocument.
    // Purely additive, same style as MIGRATION_15_16/18_19/20_21 -- no existing column touched,
    // old rows just read back with the new columns null until the next sync.
    val MIGRATION_21_22 = object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `market_stock_previews` ADD COLUMN `dailyDigestHeadline` TEXT")
            db.execSQL("ALTER TABLE `market_stock_previews` ADD COLUMN `deepAnalysisDate` TEXT")
            db.execSQL("ALTER TABLE `market_stock_previews` ADD COLUMN `deepVersion` INTEGER")
            db.execSQL("ALTER TABLE `market_stock_previews` ADD COLUMN `nextDeepDiveTriggerDate` TEXT")
            db.execSQL("ALTER TABLE `market_stock_previews` ADD COLUMN `nextDeepDiveTriggerReason` TEXT")
            db.execSQL("ALTER TABLE `market_stock_details` ADD COLUMN `dailyDigestSections` TEXT")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `market_stock_deep_dives` (
                    `symbol` TEXT NOT NULL,
                    `lastSyncedTimestamp` INTEGER NOT NULL,
                    `deepAnalysisDate` TEXT,
                    `deepVersion` INTEGER,
                    `earningsTriggered` INTEGER,
                    `sections` TEXT,
                    `nextDeepDiveTriggerDate` TEXT,
                    `nextDeepDiveTriggerReason` TEXT,
                    PRIMARY KEY(`symbol`)
                )
                """.trimIndent()
            )
        }
    }

    // Migration from Version 22 to 23: fundamentals_delta chips under the Deep Dive screen's
    // WHATS_CHANGED section. A separate migration rather than amending MIGRATION_21_22 in place --
    // that migration had already run against a real installed build during this same session (the
    // per-symbol-intelligence feature was tested before this delta-chip addition), so any device
    // already at version 22 needs an actual forward migration to pick up the new column; amending
    // an already-applied migration's SQL doesn't change what a live database already has, and Room
    // validates the live schema against the entity-derived one on every open regardless of whether
    // a migration ran -- a version-22 database missing `fundamentalsDelta` while the entity class
    // now declares it is exactly the mismatch that crashes on launch. Purely additive, same style
    // as every other migration here -- no existing column touched.
    val MIGRATION_22_23 = object : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `market_stock_deep_dives` ADD COLUMN `fundamentalsDelta` TEXT")
        }
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
        MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
        MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
        MIGRATION_21_22, MIGRATION_22_23 // 💡 Added to registry
    )
}