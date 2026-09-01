package com.ashmeet.hyperlauncher.compose

import androidx.compose.animation.AnimatedVisibility
import com.ashmeet.hyperlauncher.utils.translatedText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.ProgressListener
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import androidx.compose.ui.tooling.preview.Preview
import com.ashmeet.hyperlauncher.theme.PojavTheme

@Stable
class TaskProgressState(
    val key: String,
    initialProgress: Int = 0,
    initialMessage: String = ""
) {
    var progress by mutableIntStateOf(initialProgress)
    var message by mutableStateOf(initialMessage)
}

private val OBSERVED_PROGRESS_KEYS = listOf(
    "unpack_runtime", "download_minecraft", "download_verlist",
    "authenticate", "install_modpack", "extract_components",
    "extract_single_files", "instance_install", "data_migration",
    "copy_files", "download_content", "download_translations"
)

@Composable
fun ProgressLayoutCompose(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var taskCount by remember { mutableIntStateOf(ProgressKeeper.getTaskCount()) }
    val activeTasks = remember { mutableStateMapOf<String, TaskProgressState>() }
    val activeTaskList by remember {
        derivedStateOf { activeTasks.values.toList() }
    }

    DisposableEffect(Unit) {
        val taskCountListener = TaskCountListener { tc ->
            taskCount = tc
            false
        }
        ProgressKeeper.addTaskCountListener(taskCountListener)

        val listeners = OBSERVED_PROGRESS_KEYS.map { key ->
            val listener = object : ProgressListener {
                private var lastUpdate = 0L

                override fun onProgressStarted() {
                    activeTasks[key] = TaskProgressState(key)
                }

                override fun onProgressUpdated(progress: Int, resid: Int, vararg va: Any?) {
                    val now = System.currentTimeMillis()
                    if (now - lastUpdate < 50 && progress != 100) return
                    lastUpdate = now

                    val msg = if (resid != -1) context.getString(resid, *va)
                    else if (va.isNotEmpty() && va[0] != null) va[0].toString()
                    else ""

                    val existing = activeTasks[key]
                    if (existing != null) {
                        if (existing.progress != progress) existing.progress = progress
                        if (existing.message != msg) existing.message = msg
                    } else {
                        activeTasks[key] = TaskProgressState(key, progress, msg)
                    }
                }

                override fun onProgressEnded() {
                    activeTasks.remove(key)
                }
            }
            ProgressKeeper.addListener(key, listener)
            key to listener
        }

        onDispose {
            ProgressKeeper.removeTaskCountListener(taskCountListener)
            listeners.forEach { (key, listener) ->
                ProgressKeeper.removeListener(key, listener)
            }
        }
    }

    ProgressLayoutContent(
        taskCount = taskCount,
        activeTasks = activeTaskList,
        modifier = modifier
    )
}

@Composable
fun ProgressLayoutContent(
    taskCount: Int,
    activeTasks: List<TaskProgressState>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    if (taskCount > 0) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = translatedText(stringResource(R.string.progresslayout_tasks_in_progress, taskCount)),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeTasks, key = { it.key }) { task ->
                        TaskItem(task)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskProgressState) {
    Column {
        Text(
            text = task.message,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        TaskProgressIndicator(task)
    }
}

@Composable
fun TaskProgressIndicator(task: TaskProgressState) {
    val isIndeterminate by remember { derivedStateOf { task.progress < 0 } }
    if (!isIndeterminate) {
        LinearProgressIndicator(
            progress = { task.progress / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline,
        )
    } else {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline,
        )
    }
}

@Preview
@Composable
fun ProgressLayoutPreview() {
    PojavTheme {
        ProgressLayoutContent(
            taskCount = 2,
            activeTasks = listOf(
                TaskProgressState("download_minecraft", 50, "Downloading Minecraft..."),
                TaskProgressState("install_modpack", -1, "Installing Modpack...")
            )
        )
    }
}
