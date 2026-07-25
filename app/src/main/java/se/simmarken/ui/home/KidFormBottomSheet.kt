package se.simmarken.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.simmarken.R
import se.simmarken.ui.home.components.ColorSwatchGrid
import se.simmarken.ui.home.components.KidAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidFormBottomSheet(
    viewModel: KidFormViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val selectedColorArgb by viewModel.selectedColorArgb.collectAsStateWithLifecycle()
    val nameError by viewModel.nameError.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isReadyToSave by viewModel.isReadyToSave.collectAsStateWithLifecycle()

    val sheetTitle = if (viewModel.isEditMode) {
        stringResource(R.string.kid_form_title_edit)
    } else {
        stringResource(R.string.kid_form_title_add)
    }
    val saveLabel = if (viewModel.isEditMode) {
        stringResource(R.string.kid_form_save)
    } else {
        stringResource(R.string.kid_form_add)
    }
    val nameErrorText = when (nameError) {
        KidFormFieldError.NameEmpty -> stringResource(R.string.kid_name_error_empty)
        KidFormFieldError.NameTooLong -> stringResource(R.string.kid_name_error_too_long)
        KidFormFieldError.LoadFailed -> stringResource(R.string.kid_load_error)
        null -> null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(text = sheetTitle, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.kid_form_name_label)) },
                placeholder = { Text(stringResource(R.string.kid_form_name_placeholder)) },
                singleLine = true,
                isError = nameErrorText != null,
                supportingText = nameErrorText?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.kid_form_color_label), style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            KidAvatar(
                name = name,
                avatarColorArgb = selectedColorArgb,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ColorSwatchGrid(
                selectedColorArgb = selectedColorArgb,
                onColorSelected = viewModel::selectColor,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
                }
                Button(
                    onClick = viewModel::save,
                    enabled = isReadyToSave && !isSaving,
                ) {
                    Text(saveLabel)
                }
            }
        }
    }
}
