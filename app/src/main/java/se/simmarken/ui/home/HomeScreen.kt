package se.simmarken.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import se.simmarken.SimmarkenApplication
import se.simmarken.navigation.KidFormViewModelFactory
import se.simmarken.ui.home.components.KidAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val application = context.applicationContext as SimmarkenApplication

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Simmärken") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddSheet,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Lägg till barn",
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(uiState.kids, key = { it.id }) { kid ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KidAvatar(
                        name = kid.name,
                        avatarColorArgb = kid.avatarColorArgb,
                    )
                    Text(
                        text = kid.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }

    if (uiState.sheetState is KidSheetState.Add) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val scope = rememberCoroutineScope()
        val formViewModel: KidFormViewModel = viewModel(
            factory = KidFormViewModelFactory(application.container.kidRepository, null),
        )
        val saveCompleted by formViewModel.saveCompleted.collectAsStateWithLifecycle()

        val dismissSheet: () -> Unit = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    viewModel.closeSheet()
                }
            }
        }

        LaunchedEffect(saveCompleted) {
            if (saveCompleted) {
                dismissSheet()
            }
        }

        KidFormBottomSheet(
            viewModel = formViewModel,
            sheetState = sheetState,
            onDismiss = dismissSheet,
        )
    }
}
