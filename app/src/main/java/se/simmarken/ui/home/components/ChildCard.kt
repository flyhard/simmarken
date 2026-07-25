package se.simmarken.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.simmarken.R

@Composable
fun ChildCard(
    name: String,
    avatarColorArgb: Int,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    inProgressCount: Int = 0,
    toBuyCount: Int = 0,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val hasSubtitles = inProgressCount > 0 || toBuyCount > 0
    val minHeight = if (hasSubtitles) 80.dp else 72.dp

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onCardClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KidAvatar(
                    name = name,
                    avatarColorArgb = avatarColorArgb,
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (inProgressCount > 0) {
                        Text(
                            text = stringResource(R.string.child_in_progress, inProgressCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (toBuyCount > 0) {
                        Text(
                            text = stringResource(R.string.child_to_buy, toBuyCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Alternativ för $name",
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Redigera") },
                    onClick = {
                        menuExpanded = false
                        onEditClick()
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Ta bort",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDeleteClick()
                    },
                )
            }
        }
    }
}
