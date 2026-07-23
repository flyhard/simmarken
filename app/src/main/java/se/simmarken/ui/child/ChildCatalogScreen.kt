package se.simmarken.ui.child

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        selectedTabIndex = 0
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(childName) },
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
        if (uiState.kidMissing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Barnet hittades inte",
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
                    onTabSelected = { index ->
                        selectedTabIndex = index
                        viewModel.selectCatalogByTabIndex(index)
                    },
                )
                if (uiState.sections.isEmpty()) {
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
