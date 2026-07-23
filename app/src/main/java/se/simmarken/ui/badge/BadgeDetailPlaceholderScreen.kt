package se.simmarken.ui.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.simmarken.domain.model.BadgeVisualState
import se.simmarken.ui.components.BadgePinSize
import se.simmarken.ui.components.BadgePinVisual

private fun badgeStateLabel(visualState: BadgeVisualState): String = when (visualState) {
    BadgeVisualState.LOCKED -> "låst"
    BadgeVisualState.IN_PROGRESS -> "pågår"
    BadgeVisualState.ACHIEVED_TO_BUY -> "klar att köpa"
    BadgeVisualState.GOTTEN -> "köpt"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeDetailPlaceholderScreen(
    viewModel: BadgeDetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.nameSv) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tillbaka",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.badgeMissing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Märket hittades inte",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BadgePinVisual(
                    imageAssetPath = uiState.imageAssetPath,
                    categoryCode = uiState.categoryCode,
                    visualState = uiState.visualState,
                    progressFraction = uiState.progressFraction,
                    contentDescription = "${uiState.nameSv}, ${badgeStateLabel(uiState.visualState)}",
                    size = BadgePinSize.Detail,
                    totalRequirements = uiState.totalRequirements,
                )
                Text(
                    text = uiState.nameSv,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp),
                )
                Text(
                    text = "Checklista kommer snart",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
