package com.novaai.calorietracker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novaai.calorietracker.navigation.NovaNavGraph
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.navigation.bottomNavItems
import com.novaai.calorietracker.data.LanguageStore
import com.novaai.calorietracker.data.NovaLanguageState
import com.novaai.calorietracker.data.NovaNotifier
import com.novaai.calorietracker.data.NotificationPrefsStore
import com.novaai.calorietracker.data.ReminderScheduler
import com.novaai.calorietracker.data.ThemeStore
import com.novaai.calorietracker.ui.components.NovaBottomBar
import com.novaai.calorietracker.ui.theme.NavyDeep
import com.novaai.calorietracker.ui.theme.NovaAITheme
import com.novaai.calorietracker.ui.theme.NovaThemeState
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NovaThemeState.mode = ThemeStore.load(this)
        NovaLanguageState.language = LanguageStore.load(this)
        NovaNotifier.ensureChannel(this)
        ReminderScheduler.syncFromPrefs(this, NotificationPrefsStore.load(this))
        setContent {
            // Resolve all string resources against the saved language. The
            // snapshot state makes the whole tree recompose immediately when
            // the Settings screen changes the selection.
            val language = NovaLanguageState.language
            val localizedContext = remember(language) {
                val locale = Locale(language.tag)
                Locale.setDefault(locale)
                val config = Configuration(resources.configuration)
                config.setLocale(locale)
                createConfigurationContext(config)
            }
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalActivityResultRegistryOwner provides this
            ) {
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
}
