package com.miai.offline.ui.screens.models

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miai.offline.device.ModelRecommender
import com.miai.offline.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    onBack: () -> Unit,
    onModelSelected: (String) -> Unit,
    viewModel: ModelsViewModel = viewModel()
) {
    val modelStates by viewModel.modelStates.collectAsState()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Models", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modelStates, key = { it.model.id }) { state ->
                ModelCard(
                    state = state,
                    onDownload = { viewModel.downloadModel(state.model) },
                    onDelete = { viewModel.deleteModel(state.model) },
                    onOpenChat = { onModelSelected(state.model.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun ModelCard(
    state: ModelUiState,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onOpenChat: () -> Unit
) {
    val sizeMb = state.model.sizeBytes / (1024 * 1024)
    val sizeLabel = if (sizeMb >= 1024) String.format("%.1f GB", sizeMb / 1024.0) else "$sizeMb MB"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(state.model.displayName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        "${state.model.paramCount} • ${state.model.quantization} • $sizeLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                RecommendationBadge(state.recommendation)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(state.model.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

            Spacer(modifier = Modifier.height(12.dp))

            if (state.downloadProgress != null) {
                LinearProgressIndicator(
                    progress = state.downloadProgress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MiPink,
                    trackColor = BorderSubtle
                )
                Text(
                    "${state.downloadProgress}% download ho raha hai...",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.isDownloaded) {
                        Button(
                            onClick = onOpenChat,
                            colors = ButtonDefaults.buttonColors(containerColor = MiPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Chat Shuru Karein")
                        }
                        OutlinedButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = MiBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download ($sizeLabel)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationBadge(recommendation: ModelRecommender.Recommendation) {
    val (label, color) = when (recommendation) {
        is ModelRecommender.Recommendation.Recommended -> "Recommended" to SuccessGreen
        is ModelRecommender.Recommendation.Caution -> "Caution" to WarningAmber
        is ModelRecommender.Recommendation.NotRecommended -> "Not Suitable" to ErrorRed
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}
