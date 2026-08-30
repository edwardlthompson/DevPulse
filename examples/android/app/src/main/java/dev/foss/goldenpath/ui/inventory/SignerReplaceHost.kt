package dev.foss.goldenpath.ui.inventory

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.foss.goldenpath.R
import dev.foss.goldenpath.inventory.ApkArchiveIdentity
import dev.foss.goldenpath.inventory.AppliedUpdates
import dev.foss.goldenpath.inventory.InstalledAppsRevision
import dev.foss.goldenpath.inventory.InventoryCopy
import dev.foss.goldenpath.inventory.RefreshTrace
import dev.foss.goldenpath.inventory.SessionApkInstall
import dev.foss.goldenpath.inventory.SignerClash
import dev.foss.goldenpath.inventory.SignerReplaceHold
import dev.foss.goldenpath.inventory.SignerReplaceNext
import dev.foss.goldenpath.inventory.SignerReplaceStore
import dev.foss.goldenpath.inventory.UninstallIntent
import dev.foss.goldenpath.inventory.WelcomeNeeds
import kotlinx.coroutines.delay

@Composable
fun SignerReplaceHost() {
    val context = LocalContext.current
    val hold by SignerReplaceStore.pending.collectAsStateWithLifecycle()
    val revision by InstalledAppsRevision.revision.collectAsStateWithLifecycle(0)
    var notice by remember { mutableStateOf<Int?>(null) }
    var noticeArg by remember { mutableStateOf("") }
    var waitingInstaller by remember { mutableStateOf(false) }
    var waitingRemoval by remember { mutableStateOf(false) }
    var awayForUninstall by remember { mutableStateOf(false) }
    var leftInstaller by remember { mutableStateOf(false) }
    var seenPause by remember { mutableStateOf(false) }
    val uninstallStarted = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    var resumeOnce by remember { mutableStateOf(true) }
    var installingPkg by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        RefreshTrace.emit = { Log.i("DevPulse", it) }
        SignerReplaceStore.load(context.filesDir)
    }
    LaunchedEffect(hold?.packageName) { resumeOnce = true }
    val present = hold?.let { ApkArchiveIdentity.installed(context.packageManager, it.packageName) != null } == true
    val ready = hold?.let { SignerClash.filesReady(it.files()) } == true
    val phase = hold?.let { SignerClash.resume(present, ready) }
    fun beginInstall(held: SignerReplaceHold) {
        if (installingPkg == held.packageName) return
        if (installingPkg != null) {
            if (ApkArchiveIdentity.installed(context.packageManager, installingPkg!!) == null) return
            installingPkg = null
        }
        if (ApkArchiveIdentity.installed(context.packageManager, held.packageName) != null) return
        if (!WelcomeNeeds.ensureInstall(context)) return
        installingPkg = held.packageName
        waitingRemoval = false
        awayForUninstall = false
        leftInstaller = false
        uninstallStarted.set(false)
        finishUninstall(context, held, waiting = { waitingInstaller = it }, released = { installingPkg = null }) { res, arg ->
            notice = res
            noticeArg = arg
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) seenPause = true
            if (event != Lifecycle.Event.ON_RESUME || !seenPause) return@LifecycleEventObserver
            seenPause = false
            awayForUninstall = false
            if (uninstallStarted.get()) {
                uninstallStarted.set(false)
                val held = SignerReplaceStore.pending.value ?: return@LifecycleEventObserver
                if (ApkArchiveIdentity.installed(context.packageManager, held.packageName) != null) {
                    RefreshTrace.line("signer replace wait uninstall ${held.packageName}")
                    waitingRemoval = true
                } else {
                    beginInstall(held)
                }
                return@LifecycleEventObserver
            }
            if (waitingInstaller) leftInstaller = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(revision, waitingRemoval, hold?.packageName, ready, present, installingPkg) {
        val held = SignerReplaceStore.installable(context.filesDir) { pkg ->
            ApkArchiveIdentity.installed(context.packageManager, pkg) != null
        } ?: return@LaunchedEffect
        if (ApkArchiveIdentity.installed(context.packageManager, held.packageName) != null) {
            if (!waitingRemoval) return@LaunchedEffect
            repeat(20) {
                delay(250)
                if (ApkArchiveIdentity.installed(context.packageManager, held.packageName) == null) {
                    beginInstall(held)
                    return@LaunchedEffect
                }
            }
            return@LaunchedEffect
        }
        beginInstall(held)
    }
    LaunchedEffect(resumeOnce, hold?.packageName, phase) {
        val held = hold ?: return@LaunchedEffect
        if (!resumeOnce || phase == null) return@LaunchedEffect
        when (phase) {
            SignerReplaceNext.Confirm -> resumeOnce = false
            SignerReplaceNext.Install -> {
                resumeOnce = false
                beginInstall(held)
            }
            SignerReplaceNext.MissingFile -> {
                if (installingPkg != null && installingPkg != held.packageName) return@LaunchedEffect
                resumeOnce = false
                notice = R.string.signer_replace_missing
                SignerReplaceStore.clear(context.filesDir, packageName = held.packageName)
            }
            else -> resumeOnce = false
        }
    }
    LaunchedEffect(revision, waitingInstaller, hold?.packageName) {
        val held = hold ?: return@LaunchedEffect
        if (!waitingInstaller) return@LaunchedEffect
        if (ApkArchiveIdentity.installed(context.packageManager, held.packageName) != null) {
            AppliedUpdates.settle(held.packageName, filesDir = context.filesDir)
            SignerReplaceStore.clear(context.filesDir, packageName = held.packageName)
            waitingInstaller = false
            installingPkg = null
            RefreshTrace.line("signer replace done ${held.packageName}")
        }
    }
    if (hold != null && waitingRemoval && present && !awayForUninstall) {
        val held = hold!!
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.signer_replace_title, held.label)) },
            text = { Text(stringResource(R.string.signer_replace_waiting, held.label)) },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = {
                        waitingRemoval = false
                        SignerReplaceStore.clear(context.filesDir, deleteFiles = false, packageName = held.packageName)
                    },
                ) { Text(stringResource(R.string.signer_replace_cancel)) }
            },
        )
    } else if (hold != null && waitingInstaller && !present && leftInstaller) {
        val held = hold!!
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.signer_replace_title, held.label)) },
            text = { Text(stringResource(R.string.signer_replace_wait, held.label)) },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = {
                        waitingInstaller = false
                        installingPkg = null
                        SignerReplaceStore.clear(context.filesDir, deleteFiles = false, packageName = held.packageName)
                    },
                ) { Text(stringResource(R.string.signer_replace_cancel)) }
            },
        )
    } else if (hold != null && phase == SignerReplaceNext.Confirm && !waitingRemoval && !awayForUninstall && !waitingInstaller) {
        val held = hold!!
        val source = stringResource(InventoryCopy.sourceRes(held.source))
        AlertDialog(
            onDismissRequest = {
                if (uninstallStarted.get()) return@AlertDialog
                SignerReplaceStore.clear(context.filesDir, deleteFiles = false, packageName = held.packageName)
            },
            title = { Text(stringResource(R.string.signer_replace_title, held.label)) },
            text = { Text(stringResource(R.string.signer_replace_body, source, held.label)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!WelcomeNeeds.ensureInstall(context)) return@TextButton
                        if (!SignerClash.filesReady(held.files())) {
                            notice = R.string.signer_replace_no_space
                            SignerReplaceStore.clear(context.filesDir, packageName = held.packageName)
                            return@TextButton
                        }
                        RefreshTrace.line("signer replace uninstall ${held.packageName}")
                        seenPause = false
                        uninstallStarted.set(true)
                        if (!UninstallIntent.launch(context, held.packageName)) {
                            uninstallStarted.set(false)
                            RefreshTrace.line("signer replace uninstall missing ${held.packageName}")
                            return@TextButton
                        }
                        awayForUninstall = true
                    },
                ) { Text(stringResource(R.string.signer_replace_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { SignerReplaceStore.clear(context.filesDir, deleteFiles = false, packageName = held.packageName) }) {
                    Text(stringResource(R.string.signer_replace_cancel))
                }
            },
        )
    }
    notice?.let { res ->
        AlertDialog(
            onDismissRequest = { notice = null },
            title = { Text(hold?.label ?: noticeArg) },
            text = {
                Text(
                    if (res == R.string.signer_replace_wait || res == R.string.signer_replace_waiting) {
                        stringResource(res, noticeArg.ifEmpty { hold?.label.orEmpty() })
                    } else {
                        stringResource(res)
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { notice = null }) { Text(stringResource(R.string.about_close)) }
            },
        )
    }
}

private fun finishUninstall(
    context: android.content.Context,
    held: SignerReplaceHold,
    waiting: (Boolean) -> Unit,
    released: () -> Unit,
    notice: (Int, String) -> Unit,
) {
    val present = ApkArchiveIdentity.installed(context.packageManager, held.packageName) != null
    val ready = SignerClash.filesReady(held.files())
    when (SignerClash.afterUninstall(present, ready)) {
        SignerReplaceNext.Cancelled -> {
            RefreshTrace.line("signer replace cancelled ${held.packageName}")
            SignerReplaceStore.clear(context.filesDir, deleteFiles = false, packageName = held.packageName)
            released()
            notice(R.string.signer_replace_cancelled, held.label)
        }
        SignerReplaceNext.MissingFile, SignerReplaceNext.Confirm -> {
            RefreshTrace.line("signer replace missing ${held.packageName}")
            SignerReplaceStore.clear(context.filesDir, packageName = held.packageName)
            released()
            notice(R.string.signer_replace_missing, held.label)
        }
        SignerReplaceNext.Install -> {
            RefreshTrace.line("signer replace install ${held.packageName}")
            waiting(true)
            val ok = SessionApkInstall.commit(context, held.files())
            if (!ok) {
                waiting(false)
                released()
                notice(R.string.signer_replace_missing, held.label)
                RefreshTrace.line("signer replace install fail ${held.packageName}")
            }
        }
    }
}
