# core/learn

**Purpose:** bundled educational content for the Tutorials / "Learn" hub: market concept articles, mechanism decks, and indicator and event articles.

- **Screens:** `ui/screens/tutorials/TutorialsHubScreen.kt` (route `TUTORIALS_HUB`) and the article, deck and gauge screens beside it. Only `TutorialsGaugesViewModel` exists, plus `MetricDetailViewModel` and `GlossaryDetailViewModel`, which look articles up to decide whether to show a "Learn more" link.
- **Providers:** `LearnContentProvider` / `LearnContent.kt`, `IndicatorArticlesProvider` / `IndicatorArticles.kt`.
- **Backend dependency:** none — local/static. Content lives in `assets/learn_content.json` and `assets/indicator_articles.json`.

Both providers are plain lazily-cached singleton `object`s, not Hilt types, because most callers are stateless composables with no ViewModel. Each loads its JSON once per process through Moshi and falls back to empty content on an `IOException`. The indicator articles (35 by `metric_id` plus 5 event-only ids) are kept in their own file because they are several times larger than everything in `learn_content.json`. Route chips in the content name `PulseRoutes` constants that `PulseNavGraph` resolves, and a unit test checks that each is a real constant.
