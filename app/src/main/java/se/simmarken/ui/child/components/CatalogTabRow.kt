package se.simmarken.ui.child.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.simmarken.R

@Composable
fun CatalogTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { onTabSelected(0) },
            text = { Text(stringResource(R.string.catalog_tab_simidrott)) },
        )
        Tab(
            selected = selectedTabIndex == 1,
            onClick = { onTabSelected(1) },
            text = { Text(stringResource(R.string.catalog_tab_sls)) },
        )
    }
}
