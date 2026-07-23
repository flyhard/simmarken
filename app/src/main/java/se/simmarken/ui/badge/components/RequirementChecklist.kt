package se.simmarken.ui.badge.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.simmarken.domain.model.RequirementRowUiModel

@Composable
fun RequirementChecklist(
    requirements: List<RequirementRowUiModel>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        requirements.forEach { requirement ->
            RequirementChecklistRow(
                requirement = requirement,
                onToggle = { onToggle(requirement.id) },
            )
        }
    }
}
