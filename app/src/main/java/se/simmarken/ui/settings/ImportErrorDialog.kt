package se.simmarken.ui.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import se.simmarken.domain.export.InvalidReason

@Composable
fun ImportErrorDialog(
    reason: InvalidReason,
    onDismiss: () -> Unit,
) {
    val body = when (reason) {
        InvalidReason.InvalidFile -> "Filen är tom eller ogiltig."
        InvalidReason.UnsupportedVersion -> "Säkerhetskopian stöds inte av den här appversionen."
        InvalidReason.ParseFailed -> "Kunde inte läsa säkerhetskopian. Kontrollera att filen är giltig JSON."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import misslyckades") },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Stäng")
            }
        },
    )
}
