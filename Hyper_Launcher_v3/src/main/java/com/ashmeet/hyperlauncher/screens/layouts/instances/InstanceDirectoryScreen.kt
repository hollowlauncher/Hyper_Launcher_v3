package com.ashmeet.hyperlauncher.screens.layouts.instances

import androidx.activity.compose.rememberLauncherForActivityResult
import com.ashmeet.hyperlauncher.utils.translatedText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashmeet.hyperlauncher.components.DefaultSwitch
import com.ashmeet.hyperlauncher.components.SideRail
import com.ashmeet.hyperlauncher.screens.layouts.settings.preferences.TextInputDialog
import com.ashmeet.hyperlauncher.theme.PojavTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.utils.ModMetadataReader
import net.kdt.pojavlaunch.utils.WorldMetadataReader
import org.apache.commons.io.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthService
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ModrinthVersion
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import java.net.URL
import android.util.Log

@Composable
fun InstanceDirectoryScreen(
    onBack: () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    val selectedInstance = remember {
        if (isPreview) null else Instances.loadSelectedInstance()
    }
    val instanceRoot = remember(selectedInstance) {
        selectedInstance?.gameDirectory
    }

    InstanceDirectoryContent(
        instanceRoot = instanceRoot,
        selectedInstance = selectedInstance,
        onBack = onBack
    )
}

@Composable
fun InstanceDirectoryContent(
    instanceRoot: File?,
    selectedInstance: net.kdt.pojavlaunch.instances.Instance?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    var currentDir by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var sidebarMenuExpanded by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<File?>(null) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val instanceVersion = remember(selectedInstance) {
        selectedInstance?.let {
            if (it.versionId == "latest_release" || it.versionId == "latest_snapshot") {
                return@let null
            }

            val v = try {
                Tools.getVersionInfo(it.versionId)
            } catch (e: Exception) {
                null
            }
            if (v != null && v.inheritsFrom != null) return@let v.inheritsFrom

            val id = it.versionId
            if (id.contains("-")) {
                val lastPart = id.substringAfterLast("-")
                if (lastPart.contains(".") && lastPart.any { it.isDigit() }) {
                    return@let lastPart
                }
            }

            val regex = Regex("""1\.\d+(\.\d+)*(?:-?[a-zA-Z\d]+)?|\d+w\d+[a-z]""")
            regex.findAll(id).lastOrNull()?.value ?: id
        }
    }

    val instanceLoader = remember(selectedInstance) {
        selectedInstance?.let {
            val vId = it.versionId.lowercase()
            when {
                vId.contains("fabric") -> "fabric"
                vId.contains("forge") -> "forge"
                vId.contains("quilt") -> "quilt"
                vId.contains("neoforge") -> "neoforge"
                vId.contains("optifine") -> "optifine"
                else -> null
            }
        }
    }

    val loadFiles = { dir: File ->
        isLoading = true
        PojavApplication.sExecutorService.execute {
            try {
                val list = dir.listFiles()?.toList()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
                files = list
                currentDir = dir
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val fileName = Tools.getFileName(context, uri)
            currentDir?.let { destDir ->
                val destFile = File(destDir, fileName)
                PojavApplication.sExecutorService.execute {
                    try {
                        val progressKey = "copy_files"
                        ProgressKeeper.submitProgress(progressKey, 0, -1, "Importing $fileName...")

                        context.contentResolver.openInputStream(uri)?.use { input ->
                            destFile.outputStream().use { output ->
                                val totalSize = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
                                var bytesCopied = 0L
                                val buffer = ByteArray(8192)
                                var bytes = input.read(buffer)
                                while (bytes >= 0) {
                                    output.write(buffer, 0, bytes)
                                    bytesCopied += bytes
                                    if (totalSize > 0) {
                                        val progress = ((bytesCopied * 100) / totalSize).toInt()
                                        ProgressKeeper.submitProgress(progressKey, progress, -1, "Importing $fileName...")
                                    }
                                    bytes = input.read(buffer)
                                }
                            }
                        }

                        ProgressKeeper.submitProgress(progressKey, -1, -1)
                        loadFiles(destDir)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ProgressKeeper.submitProgress("copy_files", -1, -1)
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedTab) {
        val root = instanceRoot ?: return@LaunchedEffect
        when (selectedTab) {
            0 -> loadFiles(root)
            1 -> loadFiles(File(root, "mods"))
            2 -> loadFiles(File(root, "saves"))
            3 -> loadFiles(File(root, "resourcepacks"))
        }
        isSearchActive = false
        searchQuery = ""
    }

    if (showNewFolderDialog) {
        TextInputDialog(
            title = translatedText("New Folder"),
            initialValue = "",
            onConfirm = { name ->
                showNewFolderDialog = false
                if (name.isNotBlank()) {
                    currentDir?.let {
                        val newDir = File(it, name)
                        if (newDir.mkdirs()) {
                            loadFiles(it)
                        }
                    }
                }
            },
            onDismiss = { showNewFolderDialog = false }
        )
    }

    if (fileToRename != null) {
        TextInputDialog(
            title = translatedText("Rename"),
            initialValue = fileToRename?.name ?: "",
            onConfirm = { newName ->
                val target = fileToRename
                fileToRename = null
                if (newName.isNotBlank() && target != null) {
                    val dest = File(target.parentFile, newName)
                    if (target.renameTo(dest)) {
                        currentDir?.let { loadFiles(it) }
                    }
                }
            },
            onDismiss = { fileToRename = null }
        )
    }

    if (fileToDelete != null) {
        val target = fileToDelete!!
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete ${if (target.isDirectory) "Folder" else "File"}?") },
            text = { Text("Are you sure you want to delete \"${target.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        fileToDelete = null
                        PojavApplication.sExecutorService.execute {
                            if (target.isDirectory) {
                                try {
                                    FileUtils.deleteDirectory(target)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                target.delete()
                            }
                            currentDir?.let { loadFiles(it) }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (LauncherPreferences.PREF_LAUNCHER_BACKGROUND_PATH != null) Color.Transparent else MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box {
                SideRail(
                    onCreateNew = { sidebarMenuExpanded = true },
                    onRefresh = { currentDir?.let { loadFiles(it) } },
                    onImportModpack = { isSearchActive = !isSearchActive },
                    onBack = {
                        if (isSearchActive) {
                            isSearchActive = false
                            searchQuery = ""
                        } else {
                            val parent = currentDir?.parentFile
                            if (currentDir == instanceRoot || parent == null || instanceRoot == null) {
                                onBack()
                            } else if (currentDir?.absolutePath?.startsWith(instanceRoot.absolutePath) == true) {
                                loadFiles(parent)
                            } else {
                                onBack()
                            }
                        }
                    }
                )

                DropdownMenu(
                    expanded = sidebarMenuExpanded,
                    onDismissRequest = { sidebarMenuExpanded = false },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 72.dp, bottom = 80.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("New Folder") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            sidebarMenuExpanded = false
                            showNewFolderDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import File") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            sidebarMenuExpanded = false
                            importFileLauncher.launch("*/*")
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 16.dp, bottom = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = isSearchActive,
                        transitionSpec = {
                            (scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialScale = 0.9f
                            ) + fadeIn()) togetherWith fadeOut(animationSpec = tween(200))
                        },
                        label = "search_transition"
                    ) { active ->
                        if (active) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                placeholder = { Text("Search files...") },
                                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        } else {
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                divider = {},
                                indicator = { tabPositions ->
                                    if (selectedTab < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                            height = 3.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            ) {
                                val tabs = listOf("All", "Mods", "Saves", "Packs")
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        interactionSource = remember { MutableInteractionSource() },
                                        text = {
                                            Text(
                                                text = title,
                                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = isLoading to currentDir,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "content_transition",
                        modifier = Modifier.weight(1f)
                    ) { (loading, _) ->
                        if (loading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (filteredFiles.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) "No results found" else "This folder is empty",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    TextButton(
                                        onClick = { currentDir?.let { loadFiles(it) } },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Refresh")
                                    }

                                }
                            }
                        } else {
                            val lazyListState = rememberLazyListState()
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = filteredFiles,
                                    key = { it.absolutePath }
                                ) { file ->
                                    FileListItem(
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = tween(300),
                                            fadeOutSpec = tween(300),
                                            placementSpec = tween(300)
                                        ),
                                        file = file,
                                        selectedInstance = selectedInstance,
                                        instanceVersion = instanceVersion,
                                        instanceLoader = instanceLoader,
                                        onClick = {
                                            if (file.isDirectory) {
                                                loadFiles(file)
                                                searchQuery = ""
                                                isSearchActive = false
                                            } else {
                                                Tools.openPath(context, file, false)
                                            }
                                        },
                                        onDelete = { fileToDelete = file },
                                        onRename = { fileToRename = file },
                                        onOpenInFiles = {
                                            Tools.openPath(context, file, false)
                                        },
                                        onRefresh = { currentDir?.let { loadFiles(it) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListItem(
    modifier: Modifier = Modifier,
    file: File,
    selectedInstance: net.kdt.pojavlaunch.instances.Instance?,
    instanceVersion: String?,
    instanceLoader: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onOpenInFiles: () -> Unit,
    onRefresh: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    var modMeta by remember { mutableStateOf<ModMetadataReader.ModMetadata?>(null) }
    var worldMeta by remember { mutableStateOf<WorldMetadataReader.WorldMetadata?>(null) }

    var updateAvailable by remember { mutableStateOf<ModrinthVersion?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            val mMeta = ModMetadataReader.getMetadata(file)
            val wMeta = WorldMetadataReader.getMetadata(file)
            withContext(Dispatchers.Main) {
                modMeta = mMeta
                worldMeta = wMeta
            }

            if (mMeta != null && mMeta.name != null && mMeta.name.lowercase().contains("hyper client")) {
                withContext(Dispatchers.Main) { isCheckingUpdate = true }
                try {
                    val versions = ModrinthService.getProjectVersions("hyperclient")
                    val compatible = versions.filter { v ->
                        instanceVersion != null && v.gameVersions.contains(instanceVersion) &&
                                (instanceLoader == null || v.loaders.any { it.equals(instanceLoader, ignoreCase = true) })
                    }
                    if (compatible.isNotEmpty()) {
                        val latest = compatible.first()
                        val currentVersion = selectedInstance?.hyperClientVersion ?: mMeta.version
                        if (currentVersion == null || latest.name != currentVersion) {
                            withContext(Dispatchers.Main) { updateAvailable = latest }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("InstanceDirectory", "Failed to check update", e)
                } finally {
                    withContext(Dispatchers.Main) { isCheckingUpdate = false }
                }
            }
        }
    }

    val animatedAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer {
                alpha = animatedAlpha.value
            }
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                val iconBitmap = modMeta?.icon ?: worldMeta?.icon
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Rounded.Folder else Icons.AutoMirrored.Rounded.InsertDriveFile,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modMeta?.name ?: worldMeta?.worldName ?: file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp,
                    color = if (file.name.endsWith(".disabled")) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )

                val subtitle = when {
                    worldMeta != null -> "${worldMeta?.gameMode} • ${dateFormat.format(Date(file.lastModified()))}"
                    file.isDirectory -> "Directory"
                    else -> "${file.length() / 1024} KB • ${dateFormat.format(Date(file.lastModified()))}"
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (file.name == "hyper_instance.json" || file.name == "mojo_instance.json" || file.name == "mj_instance.json") {
                val tooltipState = rememberTooltipState()
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Critical metadata file") } },
                    state = tooltipState
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = translatedText("Metadata warning"),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(24.dp)
                            .clickable { scope.launch { tooltipState.show() } }
                    )
                }
            }

            if (file.isFile && (file.name.endsWith(".jar") || file.name.endsWith(".jar.disabled"))) {
                val isEnabled = !file.name.endsWith(".disabled")
                val isHyperClient = modMeta?.name?.lowercase()?.contains("hyper client") == true

                if (isHyperClient) {
                    IconButton(
                        onClick = {
                            if (updateAvailable == null) return@IconButton
                            isUpdating = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val url = URL(updateAvailable!!.downloadUrl)
                                    val fileName = updateAvailable!!.downloadUrl.substringAfterLast("/")
                                    val tempFile = File(file.parentFile, fileName + ".tmp")

                                    url.openStream().use { input ->
                                        tempFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }

                                    if (file.exists()) file.delete()
                                    val finalFile = File(file.parentFile, fileName)
                                    tempFile.renameTo(finalFile)

                                    selectedInstance?.let {
                                        it.hyperClientVersion = updateAvailable!!.name
                                        it.maybeWrite()
                                    }

                                    withContext(Dispatchers.Main) {
                                        onRefresh()
                                        updateAvailable = null
                                    }
                                } catch (e: Exception) {
                                    Log.e("InstanceDirectory", "Failed to update mod", e)
                                } finally {
                                    withContext(Dispatchers.Main) { isUpdating = false }
                                }
                            }
                        },
                        enabled = !isUpdating,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Update,
                                contentDescription = translatedText("Update available"),
                                tint = if (updateAvailable != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { alpha = if (updateAvailable != null) 1f else 0.3f }
                            )
                        }
                    }
                }

                DefaultSwitch(
                    checked = isEnabled,
                    onCheckedChange = {
                        val newFile = if (isEnabled) {
                            File(file.parentFile, file.name + ".disabled")
                        } else {
                            File(file.parentFile, file.name.removeSuffix(".disabled"))
                        }
                        if (file.renameTo(newFile)) {
                            onRefresh()
                        }
                    },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = translatedText("Options"),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = translatedText(stringResource(R.string.global_edit)),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = translatedText("Open in Files"),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenInFiles()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = translatedText(stringResource(R.string.global_delete)),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun InstanceDirectoryScreenPreview() {
    PojavTheme {
        InstanceDirectoryContent(
            instanceRoot = null,
            selectedInstance = null,
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun FileListItemPreview() {
    PojavTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            FileListItem(
                file = File("example_mod.jar"),
                selectedInstance = null,
                instanceVersion = null,
                instanceLoader = null,
                onClick = {},
                onDelete = {},
                onRename = {},
                onOpenInFiles = {},
                onRefresh = {}
            )
        }
    }
}
