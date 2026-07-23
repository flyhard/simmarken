package se.simmarken.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressRing(
    progressFraction: Float,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val trackColor = primary.copy(alpha = 0.2f)
    val strokeWidth = 3.dp
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
    val inset = strokeWidthPx / 2f

    Canvas(modifier = modifier) {
        val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        )

        drawArc(
            color = primary,
            startAngle = -90f,
            sweepAngle = progressFraction.coerceIn(0f, 1f) * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        )
    }
}
