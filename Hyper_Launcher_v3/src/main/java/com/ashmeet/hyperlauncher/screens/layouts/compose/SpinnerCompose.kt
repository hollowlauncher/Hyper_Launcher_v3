package com.ashmeet.hyperlauncher.screens.layouts.compose

import com.ashmeet.hyperlauncher.utils.translatedText
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.authenticator.AuthType
import net.kdt.pojavlaunch.authenticator.accounts.Account
import net.kdt.pojavlaunch.authenticator.accounts.Accounts
import net.kdt.pojavlaunch.authenticator.listener.LoginListener
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraListener
import java.io.IOException
import androidx.compose.ui.tooling.preview.Preview
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import com.ashmeet.hyperlauncher.utils.SkinUtils
import com.ashmeet.hyperlauncher.theme.PojavTheme

@Composable
fun AccountSpinnerCompose(
    modifier: Modifier = Modifier,
    hideDivider: Boolean = false
) {
    val context = LocalContext.current
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var expanded by remember { mutableStateOf(false) }

    val loginListener = remember {
        object : LoginListener {
            override fun onLoginDone(account: Account?) {
                if (account != null) {
                    Accounts.setCurrent(account)
                    ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true)
                }
            }

            override fun onLoginError(errorMessage: Throwable?) {
            }

            override fun onLoginProgress(step: Int) {
            }

            override fun setMaxLoginProgress(max: Int) {
            }
        }
    }

    val refreshAccount: (Account) -> Unit = { account ->
        if (ProgressKeeper.getTaskCount() == 0) {
            PojavApplication.sExecutorService.execute {
                val refreshAccount = account.reload() ?: return@execute
                val authType = refreshAccount.authType
                if (authType.requiresLogin() && System.currentTimeMillis() > refreshAccount.expiresAt) {
                    authType.createAuth().refreshAccount(loginListener, refreshAccount)
                }
            }
        } else {
            ProgressKeeper.waitUntilDone {
                PojavApplication.sExecutorService.execute {
                    val refreshAccount = account.reload() ?: return@execute
                    val authType = refreshAccount.authType
                    if (authType.requiresLogin() && System.currentTimeMillis() > refreshAccount.expiresAt) {
                        authType.createAuth().refreshAccount(loginListener, refreshAccount)
                    }
                }
            }
        }
    }

    val reloadAccounts: (Boolean) -> Unit = { notifyOthers ->
        PojavApplication.sExecutorService.execute {
            try {
                val loadedAccounts = Accounts.load()
                Tools.runOnUiThread {
                    accounts = loadedAccounts.accounts
                    selectedIndex = loadedAccounts.selectionIndex

                    if (selectedIndex >= 0 && selectedIndex < accounts.size) {
                        refreshAccount(accounts[selectedIndex])
                    }
                    if (notifyOthers) {
                        ExtraCore.setValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, true)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadAccounts(true)
    }

    DisposableEffect(Unit) {
        val refreshListener = object : ExtraListener<Any> {
            override fun onValueSet(key: String, value: Any): Boolean {
                reloadAccounts(false)
                return false
            }
        }

        val microsoftLoginListener = object : ExtraListener<String> {
            override fun onValueSet(key: String, value: String): Boolean {
                val backgroundLogin = AuthType.MICROSOFT.createAuth()
                backgroundLogin.createAccount(loginListener, value)
                return false
            }
        }

        val elyByLoginListener = ExtraListener<String> { key, value ->
            val backgroundLogin = AuthType.ELY_BY.createAuth()
            backgroundLogin.createAccount(loginListener, value)
            false
        }

        val mojangLoginListener = object : ExtraListener<Array<String>> {
            override fun onValueSet(key: String, value: Array<String>): Boolean {
                try {
                    val account = Accounts.create { acc: Account -> acc.username = value[0] }
                    Accounts.setCurrent(account)
                    loginListener.onLoginDone(account)
                } catch (e: IOException) {
                    loginListener.onLoginError(e)
                }
                return false
            }
        }

        ExtraCore.addExtraListener(ExtraConstants.REFRESH_ACCOUNT_SPINNER, refreshListener)
        ExtraCore.addExtraListener(ExtraConstants.MICROSOFT_LOGIN_TODO, microsoftLoginListener)
        ExtraCore.addExtraListener(ExtraConstants.ELYBY_LOGIN_TODO, elyByLoginListener)
        ExtraCore.addExtraListener(ExtraConstants.MOJANG_LOGIN_TODO, mojangLoginListener)

        onDispose {
            ExtraCore.removeExtraListenerFromValue(ExtraConstants.REFRESH_ACCOUNT_SPINNER, refreshListener)
            ExtraCore.removeExtraListenerFromValue(ExtraConstants.MICROSOFT_LOGIN_TODO, microsoftLoginListener)
            ExtraCore.removeExtraListenerFromValue(ExtraConstants.ELYBY_LOGIN_TODO, elyByLoginListener)
            ExtraCore.removeExtraListenerFromValue(ExtraConstants.MOJANG_LOGIN_TODO, mojangLoginListener)
        }
    }

    val selectedAccount = if (selectedIndex >= 0 && selectedIndex < accounts.size) accounts[selectedIndex] else null

    AccountSpinnerUI(
        selectedAccount = selectedAccount,
        accounts = accounts,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        onAddAccountClick = {
            expanded = false
            ExtraCore.setValue(ExtraConstants.SELECT_AUTH_METHOD, true)
        },
        onAccountSelected = { account ->
            expanded = false
            Accounts.setCurrent(account)
            reloadAccounts(true)
        },
        onAccountDelete = { account ->
            expanded = false
            MaterialAlertDialogBuilder(context)
                .setMessage(R.string.warning_remove_account)
                .setPositiveButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.global_delete) { _, _ ->
                    Accounts.delete(account)
                    reloadAccounts(true)
                }
                .show()
        },
        hideDivider = hideDivider,
        modifier = modifier
    )
}

@Composable
fun AccountSpinnerUI(
    selectedAccount: Account?,
    accounts: List<Account>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddAccountClick: () -> Unit,
    onAccountSelected: (Account) -> Unit,
    onAccountDelete: (Account) -> Unit,
    hideDivider: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onExpandedChange(true) },
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedAccount != null) {
                        AccountItemContent(selectedAccount)
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = translatedText(stringResource(R.string.main_add_account)),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    }
                }

                if (!hideDivider) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .width(300.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = translatedText(stringResource(R.string.main_add_account)),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                onClick = onAddAccountClick
            )

            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AccountItemContent(account)
                            }
                            IconButton(onClick = { onAccountDelete(account) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = translatedText("Delete"),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    onClick = { onAccountSelected(account) }
                )
            }
        }
    }
}

@Composable
fun AccountItemContent(account: Account) {
    val skinHead by SkinUtils.rememberSkinHead(account)

    Box(modifier = Modifier.size(32.dp)) {
        if (skinHead != null) {
            Image(
                bitmap = skinHead!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.FillBounds
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Gray, RoundedCornerShape(4.dp)))
        }

        if (account.authType != AuthType.LOCAL && account.authType.iconResource != 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                    .padding(2.dp)
            ) {
                Icon(
                    painter = painterResource(id = account.authType.iconResource),
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }

    Spacer(modifier = Modifier.width(16.dp))

    Column {
        Text(
            text = account.username,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun AccountSpinnerPreview() {
    PojavTheme {
        val account1 = remember {
            Account::class.java.getDeclaredConstructor().apply { isAccessible = true }.newInstance().apply {
                username = "Steve"
                authType = AuthType.LOCAL
            }
        }
        val account2 = remember {
            Account::class.java.getDeclaredConstructor().apply { isAccessible = true }.newInstance().apply {
                username = "MicrosoftUser"
                authType = AuthType.MICROSOFT
            }
        }

        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Selected:", color = MaterialTheme.colorScheme.onSurface)
            Box(modifier = Modifier.height(64.dp).fillMaxWidth()) {
                AccountSpinnerUI(
                    selectedAccount = account1,
                    accounts = listOf(account1, account2),
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onAddAccountClick = {},
                    onAccountSelected = {},
                    onAccountDelete = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Authenticating:", color = MaterialTheme.colorScheme.onSurface)
            Box(modifier = Modifier.height(64.dp).fillMaxWidth()) {
                AccountSpinnerUI(
                    selectedAccount = account2,
                    accounts = listOf(account1, account2),
                    expanded = false,
                    onExpandedChange = {},
                    onAddAccountClick = {},
                    onAccountSelected = {},
                    onAccountDelete = {}
                )
            }
        }
    }
}
