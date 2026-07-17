package com.novaai.calorietracker.ui.screens.foodscan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.NovaAvatar
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FoodScanScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val photoSelected = stringResource(R.string.scan_food_snackbar_photo_selected)
    val photoCaptured = stringResource(R.string.scan_food_snackbar_photo_captured)

    fun showMessage(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) showMessage(photoSelected)
    }

    val cameraUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) showMessage(photoCaptured)
    }

    fun launchCamera() {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val photoFile = File.createTempFile("food_", ".jpg", cameraDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraUri.value = uri
        cameraLauncher.launch(uri)
    }

    Scaffold(
        containerColor = NavyDeep,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDeep)
                .padding(innerPadding)
        ) {
            NovaTopBar(
                title = stringResource(R.string.scan_food_title),
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NovaAvatar(size = 96.dp)

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.scan_food_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteAlpha60,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { launchCamera() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = NavyDeep
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(text = stringResource(R.string.scan_food_take_photo), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                    border = BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(text = stringResource(R.string.scan_food_upload_gallery), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.scan_food_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteAlpha30,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
