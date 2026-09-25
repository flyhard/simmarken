package se.simmarken.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.simmarken.R
import se.simmarken.ui.settings.components.SettingsDataSection
import se.simmarken.ui.settings.components.SettingsLanguageSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    // Read through updated state so the long-lived collector below always uses the current locale.
    val shareChooserTitle by rememberUpdatedState(stringResource(R.string.export_share_chooser_title))
    val snackbarMessage = uiState.snackbarMessageRes?.let { stringResource(it) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onImportUriReceived(uri)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.shareExportUri.collect { uri ->
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(send, shareChooserTitle),
            )
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    if (uiState.showExportKidPicker && uiState.exportKidPickerState != null) {
        val picker = uiState.exportKidPickerState!!
        ExportKidPickerDialog(
            kids = picker.kids,
            selectedKidIds = picker.selectedKidIds,
            onToggleKid = viewModel::toggleExportKidSelection,
            onToggleSelectAll = viewModel::toggleExportSelectAll,
            onConfirm = viewModel::confirmExportKidPicker,
            onDismiss = viewModel::dismissExportKidPicker,
        )
    }

    uiState.importPreview?.let { preview ->
        ImportConfirmDialog(
            preview = preview,
            selectedNewKidStableIds = uiState.selectedNewKidStableIds,
            onToggleNewKid = viewModel::toggleNewKidAccepted,
            onConfirm = viewModel::confirmImport,
            onDismiss = viewModel::dismissImportPreview,
        )
    }

    uiState.importError?.let { reason ->
        ImportErrorDialog(
            reason = reason,
            onDismiss = viewModel::dismissImportError,
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_content_description),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 24.dp),
        ) {
            SettingsLanguageSection(
                selectedMode = uiState.languageMode,
                onLanguageSelected = viewModel::setLanguageMode,
            )
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
            SettingsDataSection(
                exportEnabled = uiState.kids.isNotEmpty(),
                onExportClick = viewModel::onExportClicked,
                onImportClick = { importLauncher.launch(arrayOf("application/json")) },
            )
        }
    }
}
