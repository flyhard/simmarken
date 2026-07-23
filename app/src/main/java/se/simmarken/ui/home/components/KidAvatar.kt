package se.simmarken.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.simmarken.domain.util.KidAvatarInitials
import se.simmarken.ui.theme.KidAvatarColors

@Composable
fun KidAvatar(
    name: String,
    avatarColorArgb: Int,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = Color(avatarColorArgb)
    val textColor = if (avatarColorArgb == KidAvatarColors.palette[2]) {
        Color.Black
    } else {
        Color.White
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = KidAvatarInitials.fromName(name),
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}
