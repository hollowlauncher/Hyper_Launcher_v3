package com.ashmeet.hyperlauncher.components.dialogs

import android.R
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun MarqueeText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier)
}

@Composable
fun cardColor() = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun onCardColor() = MaterialTheme.colorScheme.onSurfaceVariant

fun Modifier.fadeEdge() = this
fun Modifier.verticalScrollWithBar(state: ScrollState) = this.verticalScroll(state)

@Composable
fun ImePanContainer(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = contentAlignment, content = content)
}

@Composable
fun OwnOutlinedTextField(
    value: String,
    onValueChange: (newValue: String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = MaterialTheme.shapes.large
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        isError = isError,
        supportingText = supportingText,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = shape
    )
}

@Composable
fun SingleLineTextCheck(
) {}

@Composable
fun rememberDialogMaxHeight(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp.dp
}

@Composable
fun SimpleAlertDialog(
    title: String,
    text: String,
    confirmText: String = stringResource(R.string.ok),
    dismissText: String = stringResource(R.string.cancel),
    dismissByDialog: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (dismissByDialog) onDismiss()
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fadeEdge()
                    .verticalScrollWithBar(state = scrollState)
            ) {
                Text(text = text)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                MarqueeText(text = confirmText)
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) {
                MarqueeText(text = dismissText)
            }
        }
    )
}

@Composable
fun SideDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String? = null,
    fromRight: Boolean = false,
    startText: String? = null,
    onStartClick: (() -> Unit)? = null,
    endText: String? = null,
    onEndClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrim
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                initialOffsetX = { if (fromRight) it else -it },
                animationSpec = tween(600)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { if (fromRight) it else -it },
                animationSpec = tween(600)
            ),
            modifier = Modifier
                .align(if (fromRight) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .fillMaxHeight()
                .width(IntrinsicSize.Min)
        ) {
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .clickable(enabled = false) {}, // Consume clicks
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    title?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                        HorizontalDivider()
                    }

                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        content()
                    }

                    if (startText != null || endText != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (startText != null) {
                                TextButton(onClick = onStartClick ?: onDismissRequest) {
                                    Text(text = startText)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            if (endText != null) {
                                TextButton(onClick = onEndClick ?: onDismissRequest) {
                                    Text(text = endText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun simpleEditDialogBody(
    title: String,
    value: String,
    onValueChange: (newValue: String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    maxLines: Int = 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    extraBody: @Composable (() -> Unit)? = null,
    extraContent: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit = {}
): @Composable ColumnScope.() -> Unit = {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
    )

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fadeEdge()
            .weight(1f, fill = false)
            .verticalScrollWithBar(state = scrollState)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        extraBody?.let {
            it.invoke()
            Spacer(modifier = Modifier.size(8.dp))
        }

        val focusManager = LocalFocusManager.current

        if (singleLine) {
            SingleLineTextCheck(
            )
        }

        OwnOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            label = label,
            isError = isError,
            supportingText = supportingText,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(true)
                    onConfirm()
                }
            ),
            shape = MaterialTheme.shapes.large
        )
        extraContent?.invoke()
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun <T> SimpleListDialog(
    title: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    onDismissRequest: (selected: Boolean) -> Unit,
    current: T? = null,
    itemLayout: @Composable (item: T, isCurrent: Boolean, onClick: () -> Unit) -> Unit,
    showConfirm: Boolean = false,
    confirmText: @Composable RowScope.() -> Unit = {
        MarqueeText(text = stringResource(R.string.ok))
    }
) {
    var selectedItem: T? by remember { mutableStateOf(current) }

    Dialog(
        onDismissRequest = {
            onDismissRequest(false)
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 3.dp)
                    .heightIn(max = (maxHeight - 6.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(),
                contentColor = onCardColor(),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(16.dp))

                    val state = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge()
                            .weight(1f, fill = false),
                        state = state
                    ) {
                        items(items) { item ->
                            val isCurrent = selectedItem == item
                            itemLayout(item, isCurrent) {
                                selectedItem = item
                                if (!showConfirm && !isCurrent) {
                                    onItemSelected(item)
                                    onDismissRequest(true)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(4.dp))

                    if (showConfirm) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (selectedItem != null) {
                                    onItemSelected(selectedItem!!)
                                    onDismissRequest(true)
                                }
                            }
                        ) {
                            confirmText()
                        }
                    }
                }
            }
        }
    }
}
