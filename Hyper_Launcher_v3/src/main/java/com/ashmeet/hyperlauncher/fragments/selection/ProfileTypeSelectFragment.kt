package com.ashmeet.hyperlauncher.fragments.selection

import com.ashmeet.hyperlauncher.utils.translatedText

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.ashmeet.hyperlauncher.components.MineButton
import com.ashmeet.hyperlauncher.fragments.BTAInstallFragment
import com.ashmeet.hyperlauncher.fragments.ContentInstallerFragment
import com.ashmeet.hyperlauncher.fragments.FabricInstallFragment
import com.ashmeet.hyperlauncher.fragments.ForgeInstallFragment
import com.ashmeet.hyperlauncher.fragments.InstanceEditorFragment
import com.ashmeet.hyperlauncher.fragments.LegacyFabricInstallFragment
import com.ashmeet.hyperlauncher.fragments.NeoforgeInstallFragment
import com.ashmeet.hyperlauncher.fragments.OptiFineInstallFragment
import com.ashmeet.hyperlauncher.fragments.QuiltInstallFragment
import com.ashmeet.hyperlauncher.theme.PojavTheme
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.instances.Instances
import java.io.IOException

class ProfileTypeSelectFragment : Fragment() {
    companion object {
        const val TAG = "ProfileTypeSelectFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PojavTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ProfileTypeContent(
                            onVanillaClick = {
                                try {
                                    val instance = Instances.createDefaultInstance()
                                    Instances.setSelectedInstance(instance)
                                    Tools.swapFragment(
                                        requireActivity(),
                                        InstanceEditorFragment::class.java,
                                        InstanceEditorFragment.Companion.TAG,
                                        Bundle(1)
                                    )
                                } catch (e: IOException) {
                                    Tools.showError(context, e)
                                }
                            },
                            onOptiFineClick = {
                                Tools.swapFragment(
                                    requireActivity(),
                                    OptiFineInstallFragment::class.java,
                                    OptiFineInstallFragment.Companion.TAG,
                                    null
                                )
                            },
                            onFabricClick = {
                                Tools.swapFragment(
                                    requireActivity(),
                                    FabricInstallFragment::class.java,
                                    FabricInstallFragment.Companion.TAG,
                                    null
                                )
                            },
                            onForgeClick = {
                                Tools.swapFragment(
                                    requireActivity(),
                                    ForgeInstallFragment::class.java,
                                    ForgeInstallFragment.Companion.TAG,
                                    null
                                )
                            },
                            onModpackClick = {
                                val args = Bundle().apply {
                                    putString("type", "MODPACKS")
                                    putBoolean("bypass", true)
                                }
                                Tools.swapFragment(
                                    requireActivity(),
                                    ContentInstallerFragment::class.java,
                                    ContentInstallerFragment.Companion.TAG,
                                    args
                                )
                            },
                            onQuiltClick = {
                                Tools.swapFragment(
                                    requireActivity(),
                                    QuiltInstallFragment::class.java,
                                    QuiltInstallFragment.Companion.TAG,
                                    null
                                )
                            },
                            onBTAClick = {
                                Tools.swapFragment(
                                    requireActivity(),
                                    BTAInstallFragment::class.java,
                                    BTAInstallFragment.Companion.TAG,
                                    null
                                )
                            },
                            onNeoForgeClick = {
                                Tools.swapFragment(
                                    requireActivity(),
                                    NeoforgeInstallFragment::class.java,
                                    NeoforgeInstallFragment.Companion.TAG,
                                    null
                                )
                            },
                            onLegacyFabricClick = {
                                Tools.swapFragment(
                                    requireActivity(),
                                    LegacyFabricInstallFragment::class.java,
                                    LegacyFabricInstallFragment.Companion.TAG,
                                    null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTypeContent(
    onVanillaClick: () -> Unit,
    onOptiFineClick: () -> Unit,
    onFabricClick: () -> Unit,
    onForgeClick: () -> Unit,
    onModpackClick: () -> Unit,
    onQuiltClick: () -> Unit,
    onBTAClick: () -> Unit,
    onNeoForgeClick: () -> Unit,
    onLegacyFabricClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = translatedText(stringResource(R.string.create_profile_vanilla_like_versions)),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        MineButton(
            text = translatedText(stringResource(R.string.create_instance_vanilla)),
            onClick = onVanillaClick,
            modifier = Modifier.fillMaxWidth()
        )

        MineButton(
            text = translatedText(stringResource(R.string.mod_dl_install_optifine)),
            onClick = onOptiFineClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = translatedText(stringResource(R.string.create_profile_modded_versions)),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        MineButton(
            text = translatedText(stringResource(R.string.modloader_dl_install_fabric_instance)),
            onClick = onFabricClick,
            modifier = Modifier.fillMaxWidth()
        )

        MineButton(
            text = translatedText(stringResource(R.string.modloader_dl_install_quilt_instance)),
            onClick = onQuiltClick,
            modifier = Modifier.fillMaxWidth()
        )

        MineButton(
            text = translatedText(stringResource(R.string.modloader_dl_install_legacy_fabric_instance)),
            onClick = onLegacyFabricClick,
            modifier = Modifier.fillMaxWidth()
        )

        MineButton(
            text = translatedText(stringResource(R.string.modloader_dl_install_forge_instance)),
            onClick = onForgeClick,
            modifier = Modifier.fillMaxWidth()
        )

        MineButton(
            text = translatedText(stringResource(R.string.modloader_dl_install_neoforge_instance)),
            onClick = onNeoForgeClick,
            modifier = Modifier.fillMaxWidth()
        )

        MineButton(
            text = translatedText(stringResource(R.string.modpack_install_instance_button)),
            onClick = onModpackClick,
            modifier = Modifier.fillMaxWidth()
        )

        MineButton(
            text = translatedText(stringResource(R.string.create_bta_instance)),
            onClick = onBTAClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
