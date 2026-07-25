package se.simmarken.ui.child

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import se.simmarken.R
import se.simmarken.ui.child.components.BadgeGrid
import se.simmarken.ui.child.components.CatalogEmptyState
import se.simmarken.ui.child.components.CatalogTabRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildCatalogScreen(
    viewModel: ChildCatalogViewModel,
    onBack: () -> Unit,
    onBadgeClick: (Long) -> Unit,
) {
    val childName by viewModel.childName.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sortedCatalogs = uiState.catalogs.sortedBy { it.sortOrder }
    val selectedTabIndex = sortedCatalogs
        .indexOfFirst { it.id == uiState.selectedCatalogId }
        .coerceAtLeast(0)
    val isCatalogEmpty = uiState.sections.all { it.badges.isEmpty() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(childName) },
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
        if (uiState.kidMissing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.catalog_kid_not_found),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                CatalogTabRow(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = viewModel::selectCatalogByTabIndex,
                )
                if (isCatalogEmpty) {
                    CatalogEmptyState(modifier = Modifier.weight(1f))
                } else {
                    BadgeGrid(
                        sections = uiState.sections,
                        onBadgeClick = onBadgeClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
