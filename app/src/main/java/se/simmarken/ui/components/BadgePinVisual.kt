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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
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
    val coilSizePx = when (size) {
        BadgePinSize.Grid -> 96
        BadgePinSize.Detail -> 320
    }
    val colorFilter = badgeColorFilter(visualState)

    val boxModifier = when (size) {
        BadgePinSize.Grid -> modifier.fillMaxSize()
        BadgePinSize.Detail -> modifier.size(pinSize)
    }

    Box(
        modifier = boxModifier.padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (imageAssetPath != null) {
            val context = LocalContext.current
            val placeholderColor = BadgePlaceholderColors.forCategoryCode(categoryCode)
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$imageAssetPath")
                    .size(coilSizePx)
                    .crossfade(false)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = colorFilter,
                placeholder = ColorPainter(placeholderColor),
                error = ColorPainter(placeholderColor),
            )
        } else {
            BadgePinPlaceholder(
                categoryCode = categoryCode,
                colorFilter = colorFilter,
            )
        }

        when (visualState) {
            BadgeVisualState.IN_PROGRESS -> {
                if (totalRequirements > 0) {
                    CircularProgressRing(
                        progressFraction = progressFraction,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            BadgeVisualState.LOCKED,
            BadgeVisualState.ACHIEVED_TO_BUY,
            BadgeVisualState.GOTTEN,
            -> Unit
        }

        when (visualState) {
            BadgeVisualState.ACHIEVED_TO_BUY,
            BadgeVisualState.GOTTEN,
            -> {
                StateOverlay(
                    visualState = visualState,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
            BadgeVisualState.LOCKED,
            BadgeVisualState.IN_PROGRESS,
            -> Unit
        }
    }
}

@Composable
private fun BadgePinPlaceholder(
    categoryCode: String,
    colorFilter: ColorFilter?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                if (colorFilter != null) {
                    this.colorFilter = colorFilter
                }
            }
            .background(BadgePlaceholderColors.forCategoryCode(categoryCode)),
    )
}

private fun badgeColorFilter(visualState: BadgeVisualState): ColorFilter? = when (visualState) {
    BadgeVisualState.LOCKED,
    BadgeVisualState.IN_PROGRESS,
    -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    BadgeVisualState.ACHIEVED_TO_BUY,
    BadgeVisualState.GOTTEN,
    -> null
}
