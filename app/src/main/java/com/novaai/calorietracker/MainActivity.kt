package com.novaai.calorietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novaai.calorietracker.navigation.NovaNavGraph
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.navigation.bottomNavItems
import com.novaai.calorietracker.ui.components.NovaBottomBar
import com.novaai.calorietracker.ui.theme.NavyDeep
import com.novaai.calorietracker.ui.theme.NovaAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovaAITheme {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route
                val showBottomBar = currentRoute in bottomNavRoutes

                Scaffold(
                    containerColor = NavyDeep,
                    bottomBar = {
                        if (showBottomBar) {
                            NovaBottomBar(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NavyDeep)
                            .padding(
                                bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else androidx.compose.foundation.layout.PaddingValues().calculateBottomPadding()
                            )
                    ) {
                        NovaNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
