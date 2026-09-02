package com.ashmeet.hyperlauncher.screens.layouts.installer.components

import androidx.compose.animation.core.Animatable
import com.ashmeet.hyperlauncher.utils.translatedText
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ContentInstallerType
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ContentSource
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ModrinthProject
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ModrinthVersion
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.CardPosition
import com.ashmeet.hyperlauncher.screens.layouts.settings.layouts.SettingsCard
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.SettingsActionItem
import kotlinx.coroutines.launch

@Composable
fun ProjectItemView(
    project: ModrinthProject,
    onClick: () -> Unit
) {
    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(project.id) {
        animatedAlpha.snapTo(0f)
        animatedAlpha.animateTo(1f, animationSpec = tween(400))
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .graphicsLayer { alpha = animatedAlpha.value },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProjectIcon(project)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = translatedText(project.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp
                )
                Text(
                    text = translatedText(project.description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionItemView(
    version: ModrinthVersion,
    isCompatible: Boolean,
    isLoaderCompatible: Boolean = true,
    onClick: () -> Unit
) {
    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(version.id) {
        animatedAlpha.snapTo(0f)
        animatedAlpha.animateTo(1f, animationSpec = tween(400))
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = animatedAlpha.value },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VersionStatusBadge(version.versionType)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = version.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp
                )
                Text(
                    text = version.loaders.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isLoaderCompatible) {
                val tooltipState = rememberTooltipState()
                val scope = rememberCoroutineScope()
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Incompatible loader") } },
                    state = tooltipState
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = translatedText("Incompatible loader"),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .size(24.dp)
                            .clickable { scope.launch { tooltipState.show() } }
                    )
                }
            }
        }
    }
}

@Composable
fun VersionStatusBadge(type: String) {
    val (text, color, bgColor) = when (type.lowercase()) {
        "alpha" -> Triple("A", Color(0xFFE57373), Color(0xFFC62828).copy(alpha = 0.2f))
        "beta" -> Triple("B", Color(0xFFFFD54F), Color(0xFFF9A825).copy(alpha = 0.2f))
        else -> Triple("R", Color(0xFF81C784), Color(0xFF2E7D32).copy(alpha = 0.2f))
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SubVersionItemView(
    text: String,
    isCompatible: Boolean,
    onClick: () -> Unit
) {
    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(text) {
        animatedAlpha.snapTo(0f)
        animatedAlpha.animateTo(1f, animationSpec = tween(400))
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = animatedAlpha.value },
        shape = RoundedCornerShape(12.dp),
        color = if (isCompatible) Color(0xFF2E7D32).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontWeight = if (isCompatible) FontWeight.Bold else FontWeight.Normal,
            color = if (isCompatible) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ProjectIcon(project: ModrinthProject, size: Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (project.iconBitmap != null) {
            Image(
                bitmap = project.iconBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (!project.iconUrl.isNullOrEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.size(size * 0.5f),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Extension,
                contentDescription = null,
                modifier = Modifier.size(size * 0.6f),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsSidebar(project: ModrinthProject) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        ProjectIcon(project, size = 100.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = translatedText(project.title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = translatedText(project.description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            if (project.gallery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = translatedText("Images"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    HorizontalMultiBrowseCarousel(
                        state = rememberCarouselState { project.gallery.size },
                        preferredItemWidth = 240.dp,
                        itemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(project.gallery[index])
                                .crossfade(true)
                                .build(),
                            contentDescription = translatedText("Gallery image $index"),
                            modifier = Modifier
                                .fillMaxSize()
                                .maskClip(MaterialTheme.shapes.extraLarge),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = translatedText("Images"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = translatedText("No gallery images available."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SearchFiltersSidebar(
    instanceVersion: String?,
    instanceLoader: String?,
    selectedVersion: String?,
    selectedLoader: String?,
    selectedSource: ContentSource = ContentSource.MODRINTH,
    showLoaderFilter: Boolean = true,
    selectedType: ContentInstallerType = ContentInstallerType.MODS,
    onVersionChange: (String?) -> Unit,
    onLoaderChange: (String?) -> Unit,
    onSourceChange: (ContentSource) -> Unit,
    onImportModpack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingsCard(
                position = CardPosition.TOP,
                useSurface = true
            ) {
                FilterSourceItem(
                    currentSource = selectedSource,
                    onSourceChange = onSourceChange
                )
            }

            SettingsCard(
                position = if (showLoaderFilter) CardPosition.MIDDLE else CardPosition.BOTTOM,
                useSurface = true
            ) {
                FilterSectionItem(
                    title = translatedText("Game Version"),
                    current = selectedVersion ?: instanceVersion ?: "Any",
                    onValueChange = onVersionChange,
                    icon = Icons.Rounded.Event
                )
            }

            if (showLoaderFilter) {
                SettingsCard(
                    position = CardPosition.BOTTOM,
                    useSurface = true
                ) {
                    FilterSectionItem(
                        title = translatedText("Loader"),
                        current = selectedLoader ?: instanceLoader ?: "Any",
                        onValueChange = onLoaderChange,
                        icon = Icons.Rounded.Settings
                    )
                }
            }
        }

        if (selectedType == ContentInstallerType.MODPACKS) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                SettingsCard(
                    position = CardPosition.SINGLE,
                    useSurface = true
                ) {
                    SettingsActionItem(
                        title = translatedText("Import Modpack"),
                        summary = translatedText("Install a local modpack file (.zip, .mrpack)"),
                        icon = Icons.Rounded.FileUpload,
                        onClick = onImportModpack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FilterSourceItem(
    currentSource: ContentSource,
    onSourceChange: (ContentSource) -> Unit
) {
    var isShowingDialog by remember { mutableStateOf(false) }

    if (isShowingDialog) {
        AlertDialog(
            onDismissRequest = { isShowingDialog = false },
            title = { Text("Select Source") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContentSource.entries.forEach { source ->
                        Surface(
                            onClick = {
                                onSourceChange(source)
                                isShowingDialog = false
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (currentSource == source)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            tonalElevation = if (currentSource == source) 4.dp else 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (source == ContentSource.MODRINTH) Icons.Rounded.Language else Icons.Rounded.Extension,
                                    contentDescription = null,
                                    tint = if (currentSource == source)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = source.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (currentSource == source) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentSource == source)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (currentSource == source) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    SettingsActionItem(
        title = translatedText("Source"),
        summary = currentSource.displayName,
        icon = Icons.Rounded.Language,
        warningTooltip = if (currentSource == ContentSource.CURSEFORGE) "CurseForge support is experimental and may be unstable." else null,
        onClick = { isShowingDialog = true }
    )
}

@Composable
fun FilterSectionItem(
    title: String,
    current: String,
    onValueChange: (String?) -> Unit,
    icon: ImageVector
) {
    var isShowingDialog by remember { mutableStateOf(false) }
    var textValue by remember(current) { mutableStateOf(if (current == "Any") "" else current) }

    if (isShowingDialog) {
        AlertDialog(
            onDismissRequest = { isShowingDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter value") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onValueChange(textValue.ifBlank { null })
                        isShowingDialog = false
                    })
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(textValue.ifBlank { null })
                        isShowingDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isShowingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SettingsActionItem(
        title = title,
        summary = current,
        icon = icon,
        onClick = { isShowingDialog = true }
    )
}
