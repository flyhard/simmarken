package se.simmarken.ui.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteKidDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ta bort $name?") },
        text = {
            Text("All simmarke-framsteg för $name tas bort. Detta går inte att ångra.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Ta bort",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        },
    )
}
