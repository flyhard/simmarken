package se.simmarken.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import se.simmarken.SimmarkenApplication
import se.simmarken.navigation.ChildCatalog
import se.simmarken.navigation.KidFormViewModelFactory
import se.simmarken.ui.home.components.ChildCard
import se.simmarken.ui.home.components.DeleteKidDialog
import se.simmarken.ui.home.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavHostController,
) {
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
        if (uiState.kids.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.kids, key = { it.id }) { kid ->
                    ChildCard(
                        name = kid.name,
                        avatarColorArgb = kid.avatarColorArgb,
                        onCardClick = {
                            navController.navigate(ChildCatalog(kidId = kid.id))
                        },
                        onEditClick = {
                            viewModel.openEditSheet(kid.id)
                        },
                        onDeleteClick = {
                            viewModel.requestDelete(kid)
                        },
                    )
                }
            }
        }
    }

    if (uiState.deleteTarget == null) {
        when (val sheet = uiState.sheetState) {
            is KidSheetState.Add -> {
                KidFormSheetContent(
                    kidId = null,
                    kidRepository = application.container.kidRepository,
                    onClose = viewModel::closeSheet,
                )
            }
            is KidSheetState.Edit -> {
                KidFormSheetContent(
                    kidId = sheet.kidId,
                    kidRepository = application.container.kidRepository,
                    onClose = viewModel::closeSheet,
                )
            }
            KidSheetState.Hidden -> Unit
        }
    }

    uiState.deleteTarget?.let { kid ->
        DeleteKidDialog(
            name = kid.name,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KidFormSheetContent(
    kidId: Long?,
    kidRepository: se.simmarken.domain.repository.KidRepository,
    onClose: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val formViewModel: KidFormViewModel = viewModel(
        key = kidId?.toString() ?: "add",
        factory = KidFormViewModelFactory(kidRepository, kidId),
    )
    val saveCompleted by formViewModel.saveCompleted.collectAsStateWithLifecycle()

    val dismissSheet: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onClose()
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
