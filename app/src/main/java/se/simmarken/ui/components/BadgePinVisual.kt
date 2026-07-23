package se.simmarken.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import se.simmarken.domain.model.BadgeVisualState
import se.simmarken.ui.badge.BadgePlaceholderColors

enum class BadgePinSize {
    Grid,
    Detail,
}

@Composable
fun BadgePinVisual(
    imageAssetPath: String?,
    categoryCode: String,
    visualState: BadgeVisualState,
    progressFraction: Float,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: BadgePinSize = BadgePinSize.Grid,
    totalRequirements: Int = 0,
) {
    val pinSize: Dp = when (size) {
        BadgePinSize.Grid -> Dp.Unspecified
        BadgePinSize.Detail -> 160.dp
    }
    val isGrayscale = visualState == BadgeVisualState.LOCKED ||
        visualState == BadgeVisualState.IN_PROGRESS
    val colorFilter = if (isGrayscale) {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    } else {
        null
    }

    val boxModifier = when (size) {
        BadgePinSize.Grid -> modifier.fillMaxSize()
        BadgePinSize.Detail -> modifier.size(pinSize)
    }

    Box(
        modifier = boxModifier.padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (imageAssetPath != null) {
            AsyncImage(
                model = "file:///android_asset/$imageAssetPath",
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = colorFilter,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BadgePlaceholderColors.forCategoryCode(categoryCode)),
            )
        }

        StateOverlay(
            visualState = visualState,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}
