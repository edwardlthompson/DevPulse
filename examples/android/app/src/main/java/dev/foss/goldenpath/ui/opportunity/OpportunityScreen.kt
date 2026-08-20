package dev.foss.goldenpath.ui.opportunity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import dev.foss.goldenpath.opportunity.CategoryGap
import dev.foss.goldenpath.ui.theme.SpacingMd

@Composable
fun OpportunityScreen(
    gaps: List<CategoryGap>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text(text = stringResource(R.string.opportunity_title), style = MaterialTheme.typography.titleMedium)
        if (gaps.isEmpty()) {
            Text(text = stringResource(R.string.opportunity_empty))
        } else {
            gaps.forEach { gap ->
                Text(text = stringResource(R.string.opportunity_gap, gap.category, gap.quietCount))
            }
        }
    }
}
