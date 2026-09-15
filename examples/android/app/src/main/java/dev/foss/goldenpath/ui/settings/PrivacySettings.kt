package dev.foss.goldenpath.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.crashcapture.CrashCapture
import dev.foss.goldenpath.crashcapture.PendingCrash
import dev.foss.goldenpath.feedback.FeedbackCompose
import dev.foss.goldenpath.feedback.FeedbackGithub
import dev.foss.goldenpath.privacy.PrivacyReport
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun PrivacySettings() {
    val context = LocalContext.current
    val dir = context.filesDir
    var save by remember { mutableStateOf(CrashCapture.isEnabled(dir)) }
    var pending by remember { mutableStateOf(PendingCrash.read(dir)) }
    val uriHandler = LocalUriHandler.current
    Text(text = PrivacyReport.text())
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpacingMd)) {
        Text(text = stringResource(R.string.feedback_save_crashes), modifier = Modifier.weight(1f))
        Switch(
            checked = save,
            onCheckedChange = {
                save = it
                CrashCapture.setEnabled(it, dir)
                pending = PendingCrash.read(dir)
            },
        )
    }
    if (CrashCapture.shouldReview(save, pending)) {
        Text(text = stringResource(R.string.feedback_crash_review))
        Text(text = pending.orEmpty())
        TextButton(
            onClick = {
                PendingCrash.clear(dir)
                pending = null
            },
        ) { Text(stringResource(R.string.feedback_crash_discard)) }
    }
    val draft = FeedbackCompose.markdown("bug", pending ?: PrivacyReport.text())
    TextButton(
        onClick = {
            context.getSystemService(ClipboardManager::class.java)
                ?.setPrimaryClip(ClipData.newPlainText("feedback", draft))
        },
    ) { Text(stringResource(R.string.feedback_copy)) }
    TextButton(
        onClick = {
            runCatching { uriHandler.openUri(FeedbackGithub.issueUrl("DevPulse feedback", draft, "bug")) }
        },
    ) { Text(stringResource(R.string.feedback_open_github)) }
}
