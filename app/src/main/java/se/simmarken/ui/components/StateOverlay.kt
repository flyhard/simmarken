package se.simmarken.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import se.simmarken.domain.model.BadgeVisualState

@Composable
fun StateOverlay(
    visualState: BadgeVisualState,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector? = when (visualState) {
        BadgeVisualState.ACHIEVED_TO_BUY -> Icons.Filled.ShoppingCart
        BadgeVisualState.GOTTEN -> Icons.Filled.Check
        BadgeVisualState.LOCKED,
        BadgeVisualState.IN_PROGRESS,
        -> null
    }
    if (icon == null) return

    Box(
        modifier = modifier
            .offset(x = (-2).dp, y = (-2).dp)
            .size(20.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(12.dp),
        )
    }
}
