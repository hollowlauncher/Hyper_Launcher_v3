package com.ashmeet.hyperlauncher.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.ashmeet.hyperlauncher.R

import androidx.compose.ui.platform.ComposeView

/**
 * Bridge for Java interop.
 */
object LegacyMigratedComponentsBridge {
    @JvmStatic
    fun setProgressLayoutContent(
        view: ComposeView,
        progressText: MutableState<String>,
        isExpanded: MutableState<Boolean>,
        tasks: List<ProgressTaskState>
    ) {
        view.setContent {
            ProgressLayoutContent(
                progressText = progressText.value,
                isExpanded = isExpanded.value,
                onExpandClick = { isExpanded.value = !isExpanded.value },
                tasks = tasks
            )
        }
    }

    @JvmStatic
    fun setInstanceAdapterContent(
        view: ComposeView,
        text: String,
        icon: Any?,
        isSelected: Boolean
    ) {
        view.setContent {
            VersionProfileItem(
                text = text,
                icon = icon,
                onClick = {},
                modifier = if (isSelected) Modifier.background(Color(0x3CFFFFFF)) else Modifier
            )
        }
    }

    @JvmStatic
    fun setVersionSelectorContent(
        view: ComposeView,
        groups: List<String>,
        groupData: List<List<String>>,
        onItemClick: (Int, Int) -> Unit
    ) {
        view.setContent {
            ExpandableVersionList(
                groups = groups,
                getItems = { group -> groupData[groups.indexOf(group)] },
                groupContent = { group, _ ->
                    SimpleListItem1(text = group, onClick = {})
                },
                itemContent = { item ->
                    // Find group and item index for callback
                    var groupIdx = -1
                    var itemIdx = -1
                    for (i in groupData.indices) {
                        if (groupData[i].contains(item)) {
                            groupIdx = i
                            itemIdx = groupData[i].indexOf(item)
                            break
                        }
                    }
                    SimpleListItem1(text = item, onClick = {
                        onItemClick(groupIdx, itemIdx)
                    })
                }
            )
        }
    }
}

/**
 * Helper state for ProgressLayout migration.
 */
class ProgressTaskState(
    val progress: MutableState<Int>,
    val message: MutableState<String>,
)

/**
 * Migration of ProgressLayout content.
 */
@Composable
fun ProgressLayoutContent(
    progressText: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    tasks: List<ProgressTaskState>
) {
    ViewProgress(
        progressText = progressText,
        isExpanded = isExpanded,
        onExpandClick = onExpandClick
    ) {
        tasks.forEach { task ->
            TextProgressBar(
                progress = task.progress.value,
                text = task.message.value,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

/**
 * Migration of view_progress.xml
 * A bottom progress bar with an expandable content area above it.
 */
@Composable
fun ViewProgress(
    progressText: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // The collapsible LinearLayout (progress_linear_layout)
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_heavy))
                    .padding(top = dimensionResource(R.dimen.padding_heavy)),
                content = content
            )
        }

        // The bottom bar (ProgressBar, TextView, spinner_arrow)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen._40sdp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // progress_generic_progressbar
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(
                        width = dimensionResource(R.dimen._40sdp),
                        height = dimensionResource(R.dimen._30sdp)
                    )
                    .padding(dimensionResource(R.dimen.padding_small)),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )

            // progress_textview
            Text(
                text = progressText,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = dimensionResource(R.dimen._40sdp)),
                textAlign = TextAlign.Center,
                fontSize = with(LocalDensity.current) { dimensionResource(R.dimen._12ssp).toSp() },
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // progress_flip_arrow
            IconButton(
                onClick = onExpandClick,
                modifier = Modifier
                    .padding(end = dimensionResource(R.dimen._8sdp))
                    .size(dimensionResource(R.dimen.padding_extra_large))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.spinner_arrow),
                    contentDescription = null,
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Migration of item_simple_list_1.xml
 * A simple clickable text item with standard list padding.
 */
@Composable
fun SimpleListItem1(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = dimensionResource(R.dimen.padding_input_top),
                    bottom = dimensionResource(R.dimen.padding_input_bottom)
                ),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = with(LocalDensity.current) { dimensionResource(R.dimen._13ssp).toSp() },
            textAlign = TextAlign.Start
        )
    }
}

/**
 * Migration of item_centered_textview.xml
 * A single-line centered text view.
 */
@Composable
fun CenteredTextView(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Migration of item_centered_textview_large.xml
 * Same as CenteredTextView but with 48dp min height.
 */
@Composable
fun CenteredTextViewLarge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Migration of item_version_profile_layout.xml
 * A row with an icon and text, using selectable background.
 */
@Composable
fun VersionProfileItem(
    text: String,
    icon: Any?, // Can be Int (resource ID) or Painter
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val painter = when (icon) {
        is Int -> painterResource(id = icon)
        is Painter -> icon
        is Drawable -> BitmapPainter(icon.toBitmap().asImageBitmap())
        else -> painterResource(id = R.drawable.ic_hyper_full)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen._52sdp))
            .clickable(onClick = onClick)
            .padding(start = dimensionResource(R.dimen._17sdp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen._36sdp)),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen._19sdp)))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Migration of TextProgressBar custom view.
 */
@Composable
fun TextProgressBar(
    progress: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen._20sdp)),
        contentAlignment = Alignment.CenterStart
    ) {
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = dimensionResource(R.dimen._6sdp)),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Migration of dialog_expendable_list_view.xml
 * A wrapper for a LazyColumn that supports grouping.
 */
@Composable
fun <G, I> ExpandableVersionList(
    groups: List<G>,
    getItems: (G) -> List<I>,
    groupContent: @Composable (G, Boolean) -> Unit,
    itemContent: @Composable (I) -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen._200sdp))
    ) {
        groups.forEachIndexed { index, group ->
            item(key = "group_$index") {
                val isExpanded = expandedStates[index] ?: false
                Box(modifier = Modifier.clickable {
                    expandedStates[index] = !isExpanded
                }) {
                    groupContent(group, isExpanded)
                }
            }
            
            if (expandedStates[index] == true) {
                items(getItems(group)) { item ->
                    itemContent(item)
                }
            }
        }
    }
}
