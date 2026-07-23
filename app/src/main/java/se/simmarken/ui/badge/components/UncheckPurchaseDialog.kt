package se.simmarken.ui.badge.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UncheckPurchaseDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ta bort köpt-markering?") },
        text = {
            Text("Märket visas inte längre som köpt. Kraven du bockat av behålls.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Ta bort markering",
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
