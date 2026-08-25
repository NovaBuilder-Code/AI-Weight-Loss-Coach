package com.novaai.calorietracker.ui.screens.foodscan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.FoodItem
import com.novaai.calorietracker.data.FoodScanOutcome
import com.novaai.calorietracker.data.FoodScanResult
import com.novaai.calorietracker.data.FoodScanService
import com.novaai.calorietracker.ui.components.NovaAvatar
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun FoodScanScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val photoSelected = stringResource(R.string.scan_food_snackbar_photo_selected)
    val photoCaptured = stringResource(R.string.scan_food_snackbar_photo_captured)
    val errorTimeout = stringResource(R.string.scan_food_error_timeout)
    val errorNetwork = stringResource(R.string.scan_food_error_network)
    val errorServer = stringResource(R.string.scan_food_error_server)

    fun showMessage(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    var previewUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var analyzing by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<FoodScanResult?>(null) }
    var noFood by remember { mutableStateOf(false) }

    LaunchedEffect(previewUri) {
        previewBitmap = previewUri?.let { uri ->
            withContext(Dispatchers.IO) { decodePreviewBitmap(context, uri) }
        }
    }

    fun resetScanState() {
        analyzing = false
        scanResult = null
        noFood = false
    }

    fun analyzePhoto() {
        val bitmap = previewBitmap ?: return
        if (analyzing) return
        analyzing = true
        scanResult = null
        noFood = false
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                val jpeg = compressJpeg(bitmap.asAndroidBitmap())
                if (jpeg == null) FoodScanOutcome.ServerError else FoodScanService.analyze(jpeg)
            }
            analyzing = false
            when (outcome) {
                is FoodScanOutcome.Success ->
                    if (outcome.result.foods.isEmpty()) noFood = true else scanResult = outcome.result
                FoodScanOutcome.Timeout -> showMessage(errorTimeout)
                FoodScanOutcome.NetworkError -> showMessage(errorNetwork)
                FoodScanOutcome.ServerError -> showMessage(errorServer)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            resetScanState()
            previewUri = uri
            showMessage(photoSelected)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            resetScanState()
            previewUri = pendingCameraUri
            showMessage(photoCaptured)
        }
    }

    fun launchCamera() {
        val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val photoFile = File.createTempFile("food_", ".jpg", cameraDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        pendingCameraUri = uri
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val bitmap = previewBitmap
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = stringResource(R.string.scan_food_preview_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
                    )

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = {
                        resetScanState()
                        previewUri = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.scan_food_choose_another),
                            color = GreenPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { analyzePhoto() },
                        enabled = !analyzing,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = NavyDeep,
                            disabledContainerColor = GreenDim
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (analyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = NavyDeep,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.scan_food_analyzing),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.scan_food_analyze),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    val result = scanResult
                    if (result != null) {
                        Spacer(Modifier.height(16.dp))
                        FoodScanResultCard(result)
                    } else if (noFood) {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(NavySurface)
                                .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.scan_food_no_food),
                                color = WhiteAlpha80,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                } else {
                    NovaAvatar(size = 96.dp)

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.scan_food_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteAlpha60,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))
                }

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

/** Card showing the AI-estimated foods, total and disclaimer. */
@Composable
private fun FoodScanResultCard(result: FoodScanResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.scan_food_result_title),
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${stringResource(R.string.scan_food_result_confidence)}: " +
                    result.confidence.replaceFirstChar { it.uppercase() },
                color = GreenLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(GreenContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        result.foods.forEach { food ->
            FoodRow(food)
            Spacer(Modifier.height(12.dp))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NavyBorder)
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.scan_food_result_total),
                color = WhiteAlpha80,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${result.totalCalories} kcal",
                color = GreenPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = result.disclaimer,
            color = WhiteAlpha60,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun FoodRow(food: FoodItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = food.name,
                color = White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            if (food.estimatedPortion.isNotEmpty()) {
                Text(
                    text = food.estimatedPortion,
                    color = WhiteAlpha60,
                    fontSize = 13.sp
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${food.calories} kcal",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "${formatGrams(food.proteinG)} g protein · " +
                    "${formatGrams(food.carbsG)} g carbs · ${formatGrams(food.fatG)} g fat",
                color = WhiteAlpha60,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatGrams(v: Double): String =
    if (v == Math.floor(v) && !v.isInfinite()) v.toInt().toString()
    else v.toString().trimEnd('0').trimEnd('.')

/** Compress the already-downscaled preview bitmap to JPEG bytes for upload. */
private fun compressJpeg(bitmap: Bitmap, quality: Int = 85): ByteArray? = try {
    val out = ByteArrayOutputStream()
    if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) out.toByteArray() else null
} catch (e: Exception) {
    null
}

private fun decodePreviewBitmap(context: Context, uri: Uri, maxDim: Int = 1280): ImageBitmap? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val largest = maxOf(info.size.width, info.size.height)
            if (largest > maxDim) decoder.setTargetSampleSize(largest / maxDim)
        }.asImageBitmap()
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxDim) sampleSize *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }?.asImageBitmap()
    }
} catch (e: Exception) {
    null
}