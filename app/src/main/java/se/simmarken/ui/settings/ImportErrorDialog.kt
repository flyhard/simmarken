package se.simmarken.ui.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import se.simmarken.R
import se.simmarken.domain.export.InvalidReason

@Composable
fun ImportErrorDialog(
    reason: InvalidReason,
    onDismiss: () -> Unit,
) {
    val bodyRes = when (reason) {
        InvalidReason.InvalidFile -> R.string.import_error_invalid_file
        InvalidReason.UnsupportedVersion -> R.string.import_error_unsupported_version
        InvalidReason.ParseFailed -> R.string.import_error_parse_failed
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_error_dialog_title)) },
        text = { Text(stringResource(bodyRes)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.import_error_dialog_dismiss))
            }
        },
    )
}
