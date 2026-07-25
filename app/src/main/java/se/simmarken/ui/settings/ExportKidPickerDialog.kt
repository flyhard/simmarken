package se.simmarken.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.simmarken.data.local.entity.KidEntity

@Composable
fun ExportKidPickerDialog(
    kids: List<KidEntity>,
    selectedKidIds: Set<Long>,
    onToggleKid: (Long) -> Unit,
    onToggleSelectAll: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val allSelected = selectedKidIds.size == kids.size
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Välj barn att exportera") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = allSelected, onCheckedChange = { onToggleSelectAll() })
                    Text(text = "Alla barn", style = MaterialTheme.typography.bodyLarge)
                }
                kids.forEach { kid ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = kid.id in selectedKidIds,
                            onCheckedChange = { onToggleKid(kid.id) },
                        )
                        Text(text = kid.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedKidIds.isNotEmpty(),
            ) {
                Text("Exportera säkerhetskopia")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        },
    )
}
