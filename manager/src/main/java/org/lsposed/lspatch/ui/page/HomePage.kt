package org.lsposed.lspatch.ui.page

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.lsposed.lspatch.BuildConfig
import org.lsposed.lspatch.LSPApplication
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.util.HtmlText
import rikka.shizuku.Shizuku

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { TopBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ShizukuCard()
            Spacer(Modifier.height(16.dp))
            InfoCard(snackbarHostState)
            Spacer(Modifier.height(16.dp))
            SupportCard()
        }
    }
}

@Preview
@Composable
private fun TopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleMedium
            )
        },
    )
}

private val listener: (Int, Int) -> Unit = { _, grantResult ->
    LSPApplication.shizukuGranted = grantResult == PackageManager.PERMISSION_GRANTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShizukuCard() {
    val key = remember { true }
    LaunchedEffect(key) {
        Shizuku.addRequestPermissionResultListener(listener)
    }
    DisposableEffect(key) {
        onDispose {
            Shizuku.removeRequestPermissionResultListener(listener)
        }
    }

    ElevatedCard(
        modifier = Modifier.clickable {
            if (LSPApplication.shizukuBinderAvalable && !LSPApplication.shizukuGranted) {
                Shizuku.requestPermission(114514)
            }
        },
        containerColor = run {
            if (LSPApplication.shizukuGranted) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (LSPApplication.shizukuGranted) {
                Icon(Icons.Outlined.CheckCircle, stringResource(R.string.home_shizuku_available))
                Column(Modifier.padding(start = 20.dp)) {
                    Text(
                        text = stringResource(R.string.home_shizuku_available),
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "API " + Shizuku.getVersion(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Icon(Icons.Outlined.Warning, stringResource(R.string.home_shizuku_unavailable))
                Column(Modifier.padding(start = 20.dp)) {
                    Text(
                        text = stringResource(R.string.home_shizuku_unavailable),
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_shizuku_warning),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private val apiVersion = if (Build.VERSION.PREVIEW_SDK_INT != 0) {
    "${Build.VERSION.CODENAME} Preview (API ${Build.VERSION.PREVIEW_SDK_INT})"
} else {
    "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}

private val device = buildString {
    append(Build.MANUFACTURER[0].uppercaseChar().toString() + Build.MANUFACTURER.substring(1))
    if (Build.BRAND != Build.MANUFACTURER) {
        append(" " + Build.BRAND[0].uppercaseChar() + Build.BRAND.substring(1))
    }
    append(" " + Build.MODEL + " ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoCard(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp)
        ) {
            val contents = StringBuilder()
            val infoCardContent: @Composable (Pair<String, String>) -> Unit = { texts ->
                contents.appendLine(texts.first).appendLine(texts.second).appendLine()
                Text(text = texts.first, style = MaterialTheme.typography.bodyLarge)
                Text(text = texts.second, style = MaterialTheme.typography.bodyMedium)
            }

            infoCardContent(stringResource(R.string.home_api_version) to "${BuildConfig.API_CODE}")

            Spacer(Modifier.height(24.dp))
            infoCardContent(stringResource(R.string.home_lspatch_version) to BuildConfig.VERSION_NAME + " (${BuildConfig.VERSION_CODE})")

            Spacer(Modifier.height(24.dp))
            infoCardContent(stringResource(R.string.home_framework_version) to BuildConfig.CORE_VERSION_NAME + " (${BuildConfig.CORE_VERSION_CODE})")

            Spacer(Modifier.height(24.dp))
            infoCardContent(stringResource(R.string.home_system_version) to apiVersion)

            Spacer(Modifier.height(24.dp))
            infoCardContent(stringResource(R.string.home_device) to device)

            Spacer(Modifier.height(24.dp))
            infoCardContent(stringResource(R.string.home_system_abi) to Build.SUPPORTED_ABIS[0])

            val copiedMessage = stringResource(R.string.home_info_copied)
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("LSPatch", contents.toString()))
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                },
                content = { Text(stringResource(android.R.string.copy)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SupportCard() {
    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.home_support),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(R.string.home_description),
                style = MaterialTheme.typography.bodyMedium
            )
            HtmlText(
                stringResource(
                    R.string.home_view_source_code,
                    "<b><a href=\"https://github.com/LSPosed/LSPatch\">GitHub</a></b>",
                    "<b><a href=\"https://t.me/LSPosed\">Telegram</a></b>"
                )
            )
        }
    }
}
