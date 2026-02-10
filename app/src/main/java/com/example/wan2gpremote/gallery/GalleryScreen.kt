package com.example.wan2gpremote.gallery

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter

private enum class GalleryTab(val label: String) {
    ALL("All"),
    IMAGES("Images"),
    VIDEOS("Videos")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    onBack: () -> Unit,
    vm: GalleryViewModel = viewModel(
        factory = GalleryViewModel.Factory(GenerationHistoryStore(LocalContext.current.applicationContext))
    )
) {
    val records by vm.records.collectAsState()
    var selectedTab by remember { mutableStateOf(GalleryTab.ALL) }
    var selectedModelFilter by remember { mutableStateOf<String?>(null) }
    var selectedRecord by remember { mutableStateOf<GenerationRecord?>(null) }

    val filtered = records.filter { record ->
        val byType = when (selectedTab) {
            GalleryTab.ALL -> true
            GalleryTab.IMAGES -> record.outputType == OutputType.IMAGE
            GalleryTab.VIDEOS -> record.outputType == OutputType.VIDEO
        }
        val byModel = selectedModelFilter?.let { record.model == it } ?: true
        byType && byModel
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedRecord == null) "Generation Gallery" else "Generation Detail") },
                navigationIcon = {
                    IconButton(onClick = { if (selectedRecord == null) onBack() else selectedRecord = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (selectedRecord == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    GalleryTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.label) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(onClick = { selectedModelFilter = null }, label = { Text("All models") })
                    records.map { it.model }.distinct().forEach { model ->
                        FilterChip(
                            selected = selectedModelFilter == model,
                            onClick = { selectedModelFilter = if (selectedModelFilter == model) null else model },
                            label = { Text(model) }
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { record ->
                        RecordThumbnail(record = record, onClick = { selectedRecord = record })
                    }
                }
            }
        } else {
            RecordDetail(
                record = selectedRecord!!,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun RecordThumbnail(record: GenerationRecord, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .height(110.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (record.outputType == OutputType.IMAGE && record.localPathOrUri.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = Uri.parse(record.localPathOrUri)),
                    contentDescription = record.prompt,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(if (record.outputType == OutputType.VIDEO) "Video" else "No media", color = Color.White)
            }
        }
        Text(record.model, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(record.prompt.ifBlank { "(no prompt)" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RecordDetail(record: GenerationRecord, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Model: ${record.model}", style = MaterialTheme.typography.titleMedium)
        Text("Mode: ${record.mode}")
        Text("Prompt: ${record.prompt.ifBlank { "(none)" }}")
        Text("Server Job ID: ${record.serverJobId.ifBlank { "(none)" }}")

        if (record.outputType == OutputType.IMAGE && record.localPathOrUri.isNotBlank()) {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(record.localPathOrUri)),
                contentDescription = record.prompt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        } else if (record.outputType == OutputType.VIDEO && record.localPathOrUri.isNotBlank()) {
            VideoPlayer(
                source = record.localPathOrUri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Text("No media path recorded")
        }
    }
}

@Composable
private fun VideoPlayer(source: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember(source) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(source))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true
            }
        }
    )
}
