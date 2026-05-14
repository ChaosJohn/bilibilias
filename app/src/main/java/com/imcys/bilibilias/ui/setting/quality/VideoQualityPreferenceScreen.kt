package com.imcys.bilibilias.ui.setting.quality

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.imcys.bilibilias.ui.weight.ASIconButton
import com.imcys.bilibilias.ui.weight.ASTopAppBar
import com.imcys.bilibilias.ui.weight.AsBackIconButton
import com.imcys.bilibilias.ui.weight.BILIBILIASTopAppBarStyle
import com.imcys.bilibilias.ui.weight.tip.ASInfoTip
import com.imcys.bilibilias.weight.maybeNestedScroll
import com.imcys.bilibilias.weight.reorderable.ItemPosition
import com.imcys.bilibilias.weight.reorderable.ReorderableItem
import com.imcys.bilibilias.weight.reorderable.detectReorderAfterLongPress
import com.imcys.bilibilias.weight.reorderable.rememberReorderableLazyListState
import com.imcys.bilibilias.weight.reorderable.reorderable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object VideoQualityPreferenceRoute : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualityPreferenceScreen(
    route: VideoQualityPreferenceRoute,
    onToBack: () -> Unit,
) {
    route
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val vm = koinViewModel<VideoQualityPreferenceViewModel>()
    val qualityPreferenceOrder by vm.qualityPreferenceOrder.collectAsState()

    VideoQualityPreferenceScaffold(
        scrollBehavior = scrollBehavior,
        onToBack = onToBack,
    ) { paddingValues ->
        VideoQualityPreferenceContent(
            modifier = Modifier
                .maybeNestedScroll(scrollBehavior)
                .padding(paddingValues),
            qualityPreferenceOrder = qualityPreferenceOrder,
            onMove = { from, to ->
                vm.moveQualityItem(from.index, to.index)
            },
            onRestoreDefault = vm::restoreDefault,
        )
    }
}

@Composable
fun VideoQualityPreferenceContent(
    modifier: Modifier = Modifier,
    qualityPreferenceOrder: List<Long>,
    onMove: (ItemPosition, ItemPosition) -> Unit,
    onRestoreDefault: () -> Unit,
) {
    val fixedHeaderCount = 2
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        val fromIndex = from.index - fixedHeaderCount
        val toIndex = (to.index - fixedHeaderCount).coerceIn(0, qualityPreferenceOrder.size)

        if (fromIndex in qualityPreferenceOrder.indices && fromIndex != toIndex) {
            onMove(
                ItemPosition(fromIndex, from.key),
                ItemPosition(toIndex, to.key),
            )
        }
    })
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        state = state.listState,
        modifier = modifier
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .reorderable(state)
            .detectReorderAfterLongPress(state),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ASInfoTip {
                Text(
                    text = "解析成功后会按这里的顺序优先选择视频清晰度。你也可以长按右侧拖拽手柄调整顺序。"
                )
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = CardDefaults.shape,
                onClick = onRestoreDefault,
            ) {
                Text(text = "恢复默认顺序")
            }
        }
        items(qualityPreferenceOrder, key = { it }) { item ->
            ReorderableItem(state, key = item) { isDragging ->
                val hasTriggered = remember { mutableStateOf(false) }

                LaunchedEffect(isDragging) {
                    if (isDragging && !hasTriggered.value) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        hasTriggered.value = true
                    } else if (!isDragging) {
                        hasTriggered.value = false
                    }
                }

                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardDefaults.shape)
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.videoQualityDisplayName(),
                            modifier = Modifier.weight(1f),
                        )
                        ASIconButton(onClick = {}) {
                            Icon(Icons.Outlined.Menu, contentDescription = "长按拖拽")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualityPreferenceScaffold(
    scrollBehavior: TopAppBarScrollBehavior,
    onToBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            ASTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                scrollBehavior = scrollBehavior,
                style = BILIBILIASTopAppBarStyle.Large,
                title = { Text(text = "视频质量首选项") },
                navigationIcon = {
                    AsBackIconButton(onClick = onToBack)
                },
                alwaysDisplay = false,
            )
        },
    ) {
        content(it)
    }
}

