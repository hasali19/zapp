package dev.hasali.zapp.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.hasali.zapp.PackageSourceManifest
import dev.hasali.zapp.db.Database
import dev.hasali.zapp.db.PackageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.URL

class AddPackageSourceViewModel(private val db: Database, private val jsonFormat: Json) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun onAddClick(context: Context, url: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "Manifest url is required", Toast.LENGTH_SHORT)
                .show()
            return
        }


        withContext(Dispatchers.IO) {
            val manifest: PackageSourceManifest = try {
                val connection = URL(url).openConnection()
                jsonFormat.decodeFromStream(connection.getInputStream())
            } catch (t: Throwable) {
                Toast.makeText(context, "Failed to fetch manifest", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", t.toString())
                return@withContext
            }

            db.packageSourceDao().insertAll(
                PackageSource(
                    name = manifest.name,
                    url = url
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPackageSourceScreen(viewModel: AddPackageSourceViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Package Source") },
                navigationIcon = {
                    IconButton(onClick = { onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, "back")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
        ) {
            Column {
                var url by remember { mutableStateOf("") }

                TextField(
                    label = { Text("Manifest url") },
                    value = url,
                    onValueChange = { url = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.onAddClick(context, url)
                            onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Add")
                }
            }
        }
    }
}