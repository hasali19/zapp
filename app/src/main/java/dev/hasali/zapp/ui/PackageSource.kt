package dev.hasali.zapp.ui

import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.hasali.zapp.AppInstaller
import dev.hasali.zapp.PackageSourceManifest
import dev.hasali.zapp.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.URL

class PackageSourceViewModel(
    private val id: Int,
    private val db: Database,
    private val jsonFormat: Json,
    private val appInstaller: AppInstaller,
) {
    val source
        get() = db.packageSourceDao().getById(id)

    private val _manifest = mutableStateOf<PackageSourceManifest?>(null)

    val packages
        get() = derivedStateOf { _manifest.value?.packages }

    private val _notifications = MutableSharedFlow<String>()
    val notifications = _notifications.asSharedFlow()

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun loadManifest(url: String) {
        withContext(Dispatchers.IO) {
            _manifest.value = try {
                val connection = URL(url).openConnection()
                jsonFormat.decodeFromStream(connection.getInputStream())
            } catch (t: Throwable) {
                Log.e("MainActivity", t.toString())
                _notifications.emit("Failed to fetch manifest")
                return@withContext
            }
        }
    }

    suspend fun installPackage(pkg: PackageSourceManifest.Package) {
        var file: PackageSourceManifest.File? = null
        for (abi in Build.SUPPORTED_ABIS) {
            file = pkg.files.find { it.abi.toString() == abi }
            if (file != null) {
                break
            }
        }

        if (file == null) {
            _notifications.emit("No supported abi found in package")
            return
        }

        withContext(Dispatchers.IO) {
            val connection = URL(file.url).openConnection()
            appInstaller.install(connection.getInputStream())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageSourceScreen(viewModel: PackageSourceViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val source by viewModel.source.observeAsState()

    LaunchedEffect(viewModel.notifications) {
        viewModel.notifications.collect {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    if (source == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        LaunchedEffect(viewModel, source) {
            viewModel.loadManifest(source!!.url)
        }

        Scaffold(topBar = { TopAppBar(title = { Text(source!!.name) }) }) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                val packages by viewModel.packages
                if (packages == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn {
                        items(packages!!) { pkg ->
                            val info = try {
                                context.packageManager.getPackageInfo(pkg.packageName, 0)
                            } catch (e: NameNotFoundException) {
                                null
                            }

                            fun onInstall() {
                                coroutineScope.launch {
                                    viewModel.installPackage(pkg)
                                }
                            }

                            ListItem(
                                headlineText = { Text(pkg.name) },
                                supportingText = { Text(pkg.packageName) },
                                trailingContent = {
                                    TextButton(enabled = info == null, onClick = ::onInstall) {
                                        Text(if (info == null) "Install" else "Installed")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}