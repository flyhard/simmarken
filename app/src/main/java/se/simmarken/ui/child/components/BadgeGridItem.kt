package se.simmarken.ui.child.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.simmarken.domain.model.BadgeCellUiModel
import se.simmarken.ui.badge.BadgePlaceholderColors

@Composable
fun BadgeGridItem(
    badge: BadgeCellUiModel,
    onBadgeClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onBadgeClick(badge.id) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .background(BadgePlaceholderColors.forCategoryCode(badge.categoryCode)),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder pin box — BadgePinVisual wired in 04-03
        }
        Text(
            text = badge.nameSv,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
