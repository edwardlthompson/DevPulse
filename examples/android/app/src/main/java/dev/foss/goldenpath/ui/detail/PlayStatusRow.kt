package dev.foss.goldenpath.ui.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.R
import dev.foss.goldenpath.index.play.PlayLookup
import dev.foss.goldenpath.index.play.PlayLookupStatus
import java.text.DateFormat
import java.util.Date

@Composable
fun PlayStatusRow(
    lookup: PlayLookup,
    modifier: Modifier = Modifier,
) {
    val version = lookup.publishedVersion ?: stringResource(R.string.play_version_unknown)
    val text = if (lookup.status == PlayLookupStatus.Ok && lookup.updatedOnMs != null) {
        stringResource(
            R.string.play_status_ok,
            DateFormat.getDateInstance().format(Date(lookup.updatedOnMs)),
            version,
        )
    } else {
        stringResource(R.string.play_status_unknown)
    }
    Text(text = text, modifier = modifier.semantics { contentDescription = text })
}
