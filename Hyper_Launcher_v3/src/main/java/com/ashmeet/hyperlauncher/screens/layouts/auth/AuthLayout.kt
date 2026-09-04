package com.ashmeet.hyperlauncher.screens.layouts.auth

import android.widget.FrameLayout
import com.ashmeet.hyperlauncher.utils.translatedText
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ashmeet.hyperlauncher.skin.SkinPreview
import com.ashmeet.hyperlauncher.utils.SkinUtils
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.authenticator.AuthType
import net.kdt.pojavlaunch.authenticator.accounts.Account
import net.kdt.pojavlaunch.authenticator.accounts.Accounts
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraListener

@Composable
fun AuthLayout(
    title: String,
    onBack: (() -> Unit)? = null,
    onFragmentViewCreated: (FrameLayout) -> Unit
) {
    var currentAccount by remember {
        mutableStateOf<Account?>(try { Accounts.getCurrent() } catch (_: Exception) { null })
    }

    DisposableEffect(Unit) {
        val accountListener = ExtraListener<Any> { _, _ ->
            currentAccount = try { Accounts.getCurrent() } catch (_: Exception) { null }
            false
        }
        ExtraCore.addExtraListener(ExtraConstants.REFRESH_ACCOUNT_SPINNER, accountListener)
        onDispose {
            ExtraCore.removeExtraListenerFromValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, accountListener)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkinPreview(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                        .padding(vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    skinUrl = SkinUtils.getSkinUrl(currentAccount),
                    model = SkinUtils.getModelType(currentAccount),
                    capeUrl = when (currentAccount?.authType) {
                        AuthType.MICROSOFT -> "https://crafatar.com/capes/${currentAccount?.profileId}"
                        AuthType.ELY_BY -> "http://skinsystem.ely.by/capes/${currentAccount?.username}.png"
                        AuthType.LOCAL -> currentAccount?.capePath?.let { "file://$it" }
                        else -> null
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                ) {
                    AndroidView(
                        factory = { context ->
                            FrameLayout(context).apply {
                                id = R.id.container_fragment_auth
                                onFragmentViewCreated(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (onBack != null) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = translatedText("Back"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                }
            }
        }
    }
}
