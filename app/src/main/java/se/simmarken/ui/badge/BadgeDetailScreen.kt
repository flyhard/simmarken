package se.simmarken.ui.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.simmarken.R
import se.simmarken.domain.model.BadgeVisualState
import se.simmarken.ui.badge.components.PurchaseToggleRow
import se.simmarken.ui.badge.components.RequirementChecklist
import se.simmarken.ui.badge.components.UncheckPurchaseDialog
import se.simmarken.ui.badge.components.ZeroRequirementNote
import se.simmarken.ui.components.BadgePinSize
import se.simmarken.ui.components.BadgePinVisual

private fun badgeStateLabel(visualState: BadgeVisualState): Int = when (visualState) {
    BadgeVisualState.LOCKED -> R.string.badge_state_locked
    BadgeVisualState.IN_PROGRESS -> R.string.badge_state_in_progress
    BadgeVisualState.ACHIEVED_TO_BUY -> R.string.badge_state_achieved_to_buy
    BadgeVisualState.GOTTEN -> R.string.badge_state_gotten
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeDetailScreen(
    viewModel: BadgeDetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showUncheckPurchaseDialog) {
        UncheckPurchaseDialog(
            onConfirm = viewModel::confirmClearGotten,
            onDismiss = viewModel::dismissClearGotten,
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.nameSv) },
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
    ) { innerPadding ->
        when {
            uiState.badgeMissing -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.badge_not_found),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BadgePinVisual(
                        imageAssetPath = uiState.imageAssetPath,
                        categoryCode = uiState.categoryCode,
                        visualState = uiState.visualState,
                        progressFraction = uiState.progressFraction,
                        contentDescription = stringResource(
                            R.string.badge_pin_content_description,
                            uiState.nameSv,
                            stringResource(badgeStateLabel(uiState.visualState)),
                        ),
                        size = BadgePinSize.Detail,
                        totalRequirements = uiState.totalRequirements,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    Text(
                        text = uiState.nameSv,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                    if (uiState.totalRequirements > 0) {
                        Text(
                            text = stringResource(
                                R.string.badge_progress_subtitle,
                                uiState.achievedCount,
                                uiState.totalRequirements,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(top = if (uiState.totalRequirements > 0) 16.dp else 32.dp)
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        if (uiState.totalRequirements > 0) {
                            RequirementChecklist(
                                requirements = uiState.requirements,
                                onToggle = viewModel::toggleRequirement,
                            )
                        } else {
                            ZeroRequirementNote()
                        }
                        PurchaseToggleRow(
                            isGotten = uiState.isGotten,
                            isPurchaseEnabled = uiState.isPurchaseEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    viewModel.setGotten(true)
                                } else {
                                    viewModel.requestClearGotten()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
