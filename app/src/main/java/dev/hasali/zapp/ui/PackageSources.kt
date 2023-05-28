package dev.hasali.zapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.hasali.zapp.db.Database

class PackageSourcesViewModel(private val db: Database) {
    val sources
        @Composable get() = db.packageSourceDao().getAll().observeAsState(emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageSourcesScreen(
    viewModel: PackageSourcesViewModel,
    onAddPackageSource: () -> Unit,
    onViewPackageSource: (id: Int) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Package Sources") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPackageSource) {
                Icon(Icons.Default.Add, null)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val sources by viewModel.sources

            LazyColumn {
                items(sources) {
                    ListItem(
                        headlineText = { Text(it.name) },
                        supportingText = {
                            Text(
                                text = it.url,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.clickable {
                            onViewPackageSource(it.id)
                        },
                    )
                }
            }
        }
    }
}