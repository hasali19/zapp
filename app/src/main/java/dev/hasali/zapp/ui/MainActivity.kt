package dev.hasali.zapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import dev.hasali.zapp.AppInstaller
import dev.hasali.zapp.db.Database
import dev.hasali.zapp.repo.PackageSourceRepo
import dev.hasali.zapp.ui.theme.ZappTheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class MainActivity : ComponentActivity() {
    private val jsonFormat = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, Database::class.java, "zapp").build()
        val packageSourceRepo = PackageSourceRepo(db)

        setContent {
            val navController = rememberNavController()

            ZappTheme {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        PackageSourcesScreen(
                            viewModel = PackageSourcesViewModel(packageSourceRepo),
                            onAddPackageSource = { navController.navigate("add_package_source") },
                            onViewPackageSource = { navController.navigate("package_source/$it") },
                        )
                    }

                    composable("add_package_source") {
                        AddPackageSourceScreen(
                            viewModel = AddPackageSourceViewModel(db, jsonFormat),
                        )
                    }

                    composable("package_source/{id}") { entry ->
                        val id = entry.arguments?.getString("id")?.toInt()!!
                        PackageSourceScreen(
                            viewModel = PackageSourceViewModel(
                                id, packageSourceRepo, jsonFormat, AppInstaller(applicationContext)
                            )
                        )
                    }
                }
            }
        }
    }
}