package se.simmarken.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.simmarken.R
import se.simmarken.data.prefs.LanguageMode

@Composable
fun SettingsLanguageSection(
    selectedMode: LanguageMode,
    onLanguageSelected: (LanguageMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_language_section),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LanguageRadioRow(
            label = stringResource(R.string.language_system_default),
            selected = selectedMode == LanguageMode.SYSTEM,
            onClick = { onLanguageSelected(LanguageMode.SYSTEM) },
        )
        LanguageRadioRow(
            label = stringResource(R.string.language_swedish),
            selected = selectedMode == LanguageMode.SWEDISH,
            onClick = { onLanguageSelected(LanguageMode.SWEDISH) },
        )
        LanguageRadioRow(
            label = stringResource(R.string.language_english),
            selected = selectedMode == LanguageMode.ENGLISH,
            onClick = { onLanguageSelected(LanguageMode.ENGLISH) },
        )
    }
}
