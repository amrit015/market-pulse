package com.marketlabs.pulse.ui.compose.news

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketlabs.pulse.ui.viewmodels.NewsViewModel

@Composable
fun NewsRoute(
    scaffoldPadding: PaddingValues,
    viewModel: NewsViewModel  = hiltViewModel()
) {
    // 💡 ACTION: Safely collect states with lifecycle awareness
    val newsData by viewModel.newsState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    NewsScreen(
        data = newsData,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshNews(force = true) },
        scaffoldPadding = scaffoldPadding
    )
}