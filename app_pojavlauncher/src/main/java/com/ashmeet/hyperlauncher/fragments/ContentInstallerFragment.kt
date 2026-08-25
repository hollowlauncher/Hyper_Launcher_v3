package com.ashmeet.hyperlauncher.fragments

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.ashmeet.hyperlauncher.screens.layouts.installer.ContentInstallerScreen
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ContentInstallerType
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ContentSource
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ModrinthProject
import com.ashmeet.hyperlauncher.screens.layouts.installer.models.ModrinthVersion
import com.ashmeet.hyperlauncher.theme.PojavTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.instances.Instance
import net.kdt.pojavlaunch.instances.Instances
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences
import net.kdt.pojavlaunch.modloaders.modpacks.api.CommonApi
import net.kdt.pojavlaunch.modloaders.modpacks.api.CurseForgeService
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthService
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthApi
import net.kdt.pojavlaunch.modloaders.modpacks.api.CurseforgeApi
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModpackApi
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import java.io.File
import java.io.IOException
import java.net.URL
import kotlin.collections.map

class ContentInstallerFragment : Fragment() {

    @SuppressLint("LocalContextGetResourceValueCall")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        CurseForgeService.init(context.getString(R.string.curseforge_api_key))
                    }

                    var projects by remember { mutableStateOf<List<ModrinthProject>>(emptyList()) }
                    var isSearching by remember { mutableStateOf(false) }
                    var isProjectLoading by remember { mutableStateOf(false) }
                    var viewingProject by remember { mutableStateOf<ModrinthProject?>(null) }
                    
                    val initialType = remember {
                        arguments?.getString("type")?.let { typeStr ->
                            ContentInstallerType.entries.find { it.name == typeStr }
                        } ?: ContentInstallerType.MODS
                    }
                    val initialBypass = remember { arguments?.getBoolean("bypass", false) ?: false }
                    
                    var selectedType by remember { mutableStateOf(initialType) }
                    var selectedSource by remember {
                        mutableStateOf(if (LauncherPreferences.PREF_LAST_CONTENT_SOURCE == 1) ContentSource.CURSEFORGE else ContentSource.MODRINTH)
                    }
                    var projectVersions by remember { mutableStateOf<List<ModrinthVersion>>(emptyList()) }
                    var selectedProjectMCVersion by remember { mutableStateOf<String?>(null) }
                    var searchQuery by remember { mutableStateOf("") }

                    val instance = remember { Instances.loadSelectedInstance() }
                    val instanceVersion = remember(instance) {
                        instance?.let {
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

                    var selectedVersion by remember { mutableStateOf<String?>(null) }
                    var selectedLoader by remember { mutableStateOf<String?>(null) }

                    val instanceLoader = remember(instance) {
                        instance?.let {
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

                    var refreshTrigger by remember { mutableIntStateOf(0) }
                    LaunchedEffect(selectedType, selectedVersion, selectedLoader, searchQuery, selectedSource, refreshTrigger) {
                        if (searchQuery.isNotEmpty() && refreshTrigger == 0) {
                            delay(500)
                        }

                        isSearching = true
                        val results = if (selectedSource == ContentSource.MODRINTH) {
                            ModrinthService.search(searchQuery, selectedType, selectedVersion ?: instanceVersion, selectedLoader ?: instanceLoader)
                        } else {
                            CurseForgeService.search(searchQuery, selectedType, selectedVersion ?: instanceVersion, selectedLoader ?: instanceLoader)
                        }
                        projects = results
                        isSearching = false

                        results.forEach { project ->
                            if (!project.iconUrl.isNullOrEmpty()) {
                                scope.launch(Dispatchers.IO) {
                                    val bitmap = if (selectedSource == ContentSource.MODRINTH) {
                                        ModrinthService.loadIcon(project.iconUrl)
                                    } else {
                                        CurseForgeService.loadIcon(project.iconUrl)
                                    }
                                    withContext(Dispatchers.Main) {
                                        projects = projects.map {
                                            if (it.id == project.id) it.copy(iconBitmap = bitmap) else it
                                        }
                                        if (viewingProject?.id == project.id) {
                                            viewingProject = viewingProject?.copy(iconBitmap = bitmap)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(viewingProject?.id) {
                        val projectId = viewingProject?.id ?: run {
                            projectVersions = emptyList()
                            selectedProjectMCVersion = null
                            isProjectLoading = false
                            return@LaunchedEffect
                        }

                        projectVersions = emptyList()
                        selectedProjectMCVersion = null
                        isProjectLoading = true

                        try {
                            val detailsDeferred = async(Dispatchers.IO) {
                                if (selectedSource == ContentSource.MODRINTH) {
                                    ModrinthService.getProjectDetails(projectId)
                                } else {
                                    CurseForgeService.getProjectDetails(projectId)
                                }
                            }

                            val versionsDeferred = async(Dispatchers.IO) {
                                if (selectedSource == ContentSource.MODRINTH) {
                                    ModrinthService.getProjectVersions(projectId)
                                } else {
                                    CurseForgeService.getProjectVersions(projectId)
                                }
                            }

                            val details = detailsDeferred.await()
                            val versions = versionsDeferred.await()

                            withContext(Dispatchers.Main) {
                                viewingProject = viewingProject?.copy(
                                    fullDescription = details?.fullDescription,
                                    gallery = details?.gallery ?: emptyList()
                                )
                                projectVersions = versions
                                isProjectLoading = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProjectLoading = false
                            }
                        }
                    }

                    val availableProjectMCVersions = remember(projectVersions) {
                        projectVersions.flatMap { it.gameVersions }.distinct().sortedDescending()
                    }

                    ContentInstallerScreen(
                        onBack = { Tools.removeCurrentFragment(requireActivity()) },
                        onSearch = { query, type, version, loader, source ->
                            if (selectedType != type || selectedSource != source) {
                                projects = emptyList()
                            }

                            if (selectedSource != source) {
                                LauncherPreferences.prefs.edit {
                                    putInt("last_content_source", if (source == ContentSource.CURSEFORGE) 1 else 0)
                                }
                                LauncherPreferences.PREF_LAST_CONTENT_SOURCE = if (source == ContentSource.CURSEFORGE) 1 else 0
                            }

                            searchQuery = query
                            selectedType = type
                            selectedVersion = version
                            selectedLoader = loader
                            selectedSource = source
                        },
                        onProjectClick = { project ->
                            viewingProject = project
                        },
                        onVersionClick = { version ->
                            if (instance == null) {
                                Toast.makeText(requireContext(), "No instance selected", Toast.LENGTH_SHORT).show()
                                return@ContentInstallerScreen
                            }
                            scope.launch(Dispatchers.IO) {
                                val progressKey = "download_content"
                                if (selectedType == ContentInstallerType.MODPACKS) {
                                    try {
                                        val modpackApi = if (selectedSource == ContentSource.MODRINTH) {
                                            ModrinthApi()
                                        } else {
                                            CurseforgeApi(getString(R.string.curseforge_api_key))
                                        }

                                        val modItem = ModItem(
                                            if (selectedSource == ContentSource.MODRINTH) CommonApi.PACK_MODRINTH.toInt() else CommonApi.PACK_CURSEFORGE.toInt(),
                                            true,
                                            viewingProject?.id,
                                            viewingProject?.title,
                                            viewingProject?.description,
                                            viewingProject?.iconUrl
                                        )
                                        val modDetail = modpackApi.getModDetails(modItem)
                                        val selectedVersionIndex = modDetail.versionUrls.indexOfFirst {
                                            it == version.downloadUrl || it.contains(version.id)
                                        }

                                        if (selectedVersionIndex != -1) {
                                            withContext(Dispatchers.Main) {
                                                modpackApi.handleModpackInstallation(requireContext(), modDetail, selectedVersionIndex)
                                            }
                                        } else {
                                            performDirectDownload(version, instance, selectedType, progressKey)
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(requireContext(), "Modpack install failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else if (selectedType == ContentInstallerType.WORLDS) {
                                    installWorld(version, instance, progressKey)
                                } else {
                                    performDirectDownload(version, instance, selectedType, progressKey)
                                }
                            }
                        },
                        projects = projects,
                        isLoading = if (viewingProject != null) isProjectLoading else isSearching,
                        selectedVersion = selectedVersion,
                        selectedLoader = selectedLoader,
                        selectedSource = selectedSource,
                        instanceVersion = instanceVersion,
                        instanceLoader = instanceLoader,
                        viewingProject = viewingProject,
                        selectedType = selectedType,
                        projectVersions = projectVersions,
                        availableProjectMCVersions = availableProjectMCVersions,
                        selectedProjectMCVersion = selectedProjectMCVersion,
                        initialBypassWarning = initialBypass,
                        onProjectMCVersionClick = { selectedProjectMCVersion = it.ifEmpty { null } },
                        onBackToProjects = {
                            viewingProject = null
                            projectVersions = emptyList()
                            selectedProjectMCVersion = null
                        },
                        onRefresh = { refreshTrigger++ },
                        onImportModpack = {
                            importLauncher.launch("*/*")
                        }
                    )
                }
            }
        }
    }

    private suspend fun installWorld(version: ModrinthVersion, instance: Instance, progressKey: String) {
        try {
            val savesFolder = File(instance.gameDirectory, "saves")
            savesFolder.mkdirs()
            val fileName = version.downloadUrl.substringAfterLast("/")
            val tempZip = File(instance.gameDirectory, "temp_world.zip")

            ProgressKeeper.submitProgress(progressKey, 0, -1, "Downloading $fileName...")

            withContext(Dispatchers.IO) {
                val url = URL(version.downloadUrl)
                val connection = url.openConnection()
                connection.connect()
                val totalSize = connection.contentLength.toLong()

                connection.getInputStream().use { input ->
                    tempZip.outputStream().use { output ->
                        var bytesCopied = 0L
                        val buffer = ByteArray(8192)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            if (totalSize > 0) {
                                val progress = ((bytesCopied * 100) / totalSize).toInt()
                                ProgressKeeper.submitProgress(progressKey, progress, -1, "Downloading $fileName...")
                            }
                            bytes = input.read(buffer)
                        }
                    }
                }

                ProgressKeeper.submitProgress(progressKey, 100, -1, "Extracting world...")

                java.util.zip.ZipFile(tempZip).use { zip ->
                    val entries = zip.entries()
                    var levelDatEntry: java.util.zip.ZipEntry? = null
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.name.endsWith("level.dat")) {
                            levelDatEntry = entry
                            break
                        }
                    }

                    if (levelDatEntry == null) throw IOException("No level.dat found in zip")

                    val worldPath = levelDatEntry.name.substringBeforeLast("level.dat")
                    val worldFolderName = if (worldPath.isEmpty()) {
                        fileName.substringBeforeLast(".")
                    } else {
                        worldPath.removeSuffix("/").substringAfterLast("/")
                    }
                    
                    val finalWorldDir = File(savesFolder, worldFolderName)
                    finalWorldDir.mkdirs()

                    val extractEntries = zip.entries()
                    while (extractEntries.hasMoreElements()) {
                        val entry = extractEntries.nextElement()
                        if (entry.name.startsWith(worldPath)) {
                            val relativePath = entry.name.substring(worldPath.length)
                            if (relativePath.isEmpty()) continue
                            
                            val destFile = File(finalWorldDir, relativePath)
                            if (entry.isDirectory) {
                                destFile.mkdirs()
                            } else {
                                destFile.parentFile?.mkdirs()
                                zip.getInputStream(entry).use { input ->
                                    destFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                        }
                    }
                }
                tempZip.delete()
            }

            ProgressKeeper.submitProgress(progressKey, -1, -1)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Installed World: ${version.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            ProgressKeeper.submitProgress(progressKey, -1, -1)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Failed to install world: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun performDirectDownload(version: ModrinthVersion, instance: Instance, type: ContentInstallerType, progressKey: String) {
        try {
            val destFolder = when (type) {
                ContentInstallerType.MODS -> File(instance.gameDirectory, "mods")
                ContentInstallerType.RESOURCEPACKS -> File(instance.gameDirectory, "resourcepacks")
                ContentInstallerType.SHADERS -> File(instance.gameDirectory, "shaderpacks")
                else -> File(instance.gameDirectory, "downloads")
            }
            destFolder.mkdirs()
            val fileName = version.downloadUrl.substringAfterLast("/")
            val destFile = File(destFolder, fileName)

            ProgressKeeper.submitProgress(progressKey, 0, -1, "Downloading $fileName...")

            withContext(Dispatchers.IO) {
                val url = URL(version.downloadUrl)
                val connection = url.openConnection()
                connection.connect()
                val totalSize = connection.contentLength.toLong()

                connection.getInputStream().use { input ->
                    destFile.outputStream().use { output ->
                        var bytesCopied = 0L
                        val buffer = ByteArray(8192)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            if (totalSize > 0) {
                                val progress = ((bytesCopied * 100) / totalSize).toInt()
                                ProgressKeeper.submitProgress(progressKey, progress, -1, "Downloading $fileName...")
                            }
                            bytes = input.read(buffer)
                        }
                    }
                }
            }

            ProgressKeeper.submitProgress(progressKey, -1, -1)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Installed ${version.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            ProgressKeeper.submitProgress(progressKey, -1, -1)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Failed to install: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        val context = requireContext()
        val contentResolver = context.contentResolver
        PojavApplication.sExecutorService.execute {
            performLocalInstall(uri, context, contentResolver)
        }
    }

    private fun performLocalInstall(uri: Uri, context: Context, contentResolver: ContentResolver) {
        val fileName = Tools.getFileName(context, uri) ?: return
        val outFile = File(Tools.DIR_CACHE, "$fileName.cf")
        val progressKey = "install_modpack"
        ProgressKeeper.submitProgress(progressKey, 0, -1, "Caching modpack...")
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return
        } catch (e: IOException) {
            Tools.showErrorRemote("Error", e)
            ProgressKeeper.submitProgress(progressKey, -1, -1)
            return
        }

        try {
            val modpackApi: ModpackApi =
                CommonApi(getString(R.string.curseforge_api_key))
            modpackApi.installLocalModpack(fileName, outFile, null)
        } catch (e: IOException) {
            Tools.showErrorRemote("Error", e)
        } finally {
            outFile.delete()
            ProgressKeeper.submitProgress(progressKey, -1, -1)
        }
    }

    companion object {
        const val TAG = "ContentInstallerFragment"
    }
}
