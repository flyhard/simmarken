package se.simmarken.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.simmarken.domain.export.ImportPreview

@Composable
fun ImportConfirmDialog(
    preview: ImportPreview,
    selectedNewKidStableIds: Set<String>,
    onToggleNewKid: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val canImport = preview.updateCount > 0 || selectedNewKidStableIds.isNotEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importera säkerhetskopia?") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (preview.updateCount > 0) {
                    Text(
                        text = "${preview.updateCount} framstegsrader uppdateras för befintliga barn.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (preview.newKids.isNotEmpty()) {
                    Text(
                        text = "Nya barn i filen",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                    preview.newKids.forEach { kid ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = kid.stableId in selectedNewKidStableIds,
                                onCheckedChange = { onToggleNewKid(kid.stableId) },
                            )
                            Text(text = kid.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                if (preview.updateCount == 0 && preview.newKids.isEmpty()) {
                    Text(
                        text = "Inga ändringar att importera.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (preview.skippedRowCount > 0) {
                    Text(
                        text = "${preview.skippedRowCount} rader hoppades över (okända märken).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = canImport) {
                Text("Importera säkerhetskopia")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Importera inte")
            }
        },
    )
}
