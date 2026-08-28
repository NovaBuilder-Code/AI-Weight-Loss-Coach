package com.novaai.calorietracker.ui.screens.foodscan

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.CalorieStore
import com.novaai.calorietracker.data.FoodEditDraft
import com.novaai.calorietracker.data.FoodItem
import com.novaai.calorietracker.data.FoodScanEdit
import com.novaai.calorietracker.data.FoodScanOutcome
import com.novaai.calorietracker.data.FoodScanResult
import com.novaai.calorietracker.data.FoodScanCameraHandoff
import com.novaai.calorietracker.data.FoodScanService
import com.novaai.calorietracker.ui.components.NovaAvatar
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun FoodScanScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val photoSelected = stringResource(R.string.scan_food_snackbar_photo_selected)
    val photoCaptured = stringResource(R.string.scan_food_snackbar_photo_captured)
    val errorTimeout = stringResource(R.string.scan_food_error_timeout)
    val errorNetwork = stringResource(R.string.scan_food_error_network)
    val errorServer = stringResource(R.string.scan_food_error_server)
    val errorImage = stringResource(R.string.scan_food_error_image)
    val loggedMessage = stringResource(R.string.scan_food_logged_snackbar)

    fun showMessage(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    var previewUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    var previewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var analyzing by remember { mutableStateOf(false) }
    var analysisInFlight by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<FoodScanResult?>(null) }
    var noFood by remember { mutableStateOf(false) }
    var logged by remember { mutableStateOf(false) }
    var analyzeError by remember { mutableStateOf<String?>(null) }
    var analyzeGeneration by remember { mutableIntStateOf(0) }
    var decodeGeneration by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(FoodScanCameraHandoff.LOG_TAG, "resume_after_camera")
                val path = pendingCameraPath
                val uri = pendingCameraUri
                val exists = path != null && File(path).exists()
                val length = photoContentLength(context, uri, path)
                Log.d(
                    FoodScanCameraHandoff.LOG_TAG,
                    "file_check exists=$exists length=$length path=$path"
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun resetScanState() {
        analyzeGeneration++
        analyzing = false
        analysisInFlight = false
        scanResult = null
        noFood = false
        logged = false
        analyzeError = null
    }

    fun clearPhoto() {
        resetScanState()
        previewUri = null
    }

    fun analyzePhoto(bitmapOverride: ImageBitmap? = null) {
        val bitmap = bitmapOverride ?: previewBitmap ?: run {
            val uri = previewUri
            if (uri != null) {
                analyzeError = null
                analyzing = true
                decodeGeneration++
            }
            return
        }
        if (analysisInFlight) {
            Log.d(FoodScanCameraHandoff.LOG_TAG, "startAnalysis skipped (in-flight)")
            return
        }
        analysisInFlight = true
        analyzing = true
        scanResult = null
        noFood = false
        logged = false
        analyzeError = null
        val gen = analyzeGeneration
        Log.d(FoodScanCameraHandoff.LOG_TAG, "startAnalysis")
        Log.d(FoodScanCameraHandoff.LOG_TAG, "startAnalysis_called")
        scope.launch {
            val outcome = withContext(Dispatchers.IO) {
                val jpeg = compressJpeg(bitmap.asAndroidBitmap())
                if (jpeg == null) {
                    Log.d(FoodScanCameraHandoff.LOG_TAG, "startAnalysis compress fail")
                    null
                } else FoodScanService.analyze(jpeg)
            }
            if (gen != analyzeGeneration) {
                Log.d(FoodScanCameraHandoff.LOG_TAG, "startAnalysis stale generation")
                return@launch
            }
            analysisInFlight = false
            analyzing = false
            when (outcome) {
                null -> analyzeError = errorImage
                is FoodScanOutcome.Success ->
                    if (outcome.result.foods.isEmpty()) noFood = true else scanResult = outcome.result
                FoodScanOutcome.Timeout -> analyzeError = errorTimeout
                FoodScanOutcome.NetworkError -> analyzeError = errorNetwork
                FoodScanOutcome.ServerError -> analyzeError = errorServer
            }
            Log.d(FoodScanCameraHandoff.LOG_TAG, "startAnalysis done outcome=${outcome?.javaClass?.simpleName}")
        }
    }

    LaunchedEffect(previewUri, decodeGeneration) {
        val uri = previewUri
        if (uri == null) {
            previewBitmap = null
            return@LaunchedEffect
        }
        analyzing = true
        analyzeError = null
        Log.d(FoodScanCameraHandoff.LOG_TAG, "decode start uri=$uri")
        Log.d(FoodScanCameraHandoff.LOG_TAG, "decode_started uri=$uri")
        val pathForUri = if (uri == pendingCameraUri) pendingCameraPath else null
        val (decoded, length) = withContext(Dispatchers.IO) {
            val bytes = if (pathForUri != null) {
                waitForPhotoBytes(context, uri, pathForUri)
            } else {
                photoContentLength(context, uri, null)
            }
            val exists = pathForUri != null && File(pathForUri).exists()
            Log.d(FoodScanCameraHandoff.LOG_TAG, "file exists/length=$bytes")
            Log.d(
                FoodScanCameraHandoff.LOG_TAG,
                "file_check exists=$exists length=$bytes path=$pathForUri"
            )
            decodePreviewBitmap(context, uri) to bytes
        }
        if (previewUri != uri) {
            Log.d(FoodScanCameraHandoff.LOG_TAG, "decode stale uri")
            return@LaunchedEffect
        }
        if (decoded == null) {
            Log.d(FoodScanCameraHandoff.LOG_TAG, "decode fail")
            val reason = if (length <= 0L) "empty_or_missing length=$length" else "null_bitmap"
            Log.d(FoodScanCameraHandoff.LOG_TAG, "decode_failure reason=$reason")
            previewBitmap = null
            analyzing = false
            analyzeError = errorImage
        } else {
            Log.d(FoodScanCameraHandoff.LOG_TAG, "decode ok")
            Log.d(FoodScanCameraHandoff.LOG_TAG, "decode_success")
            previewBitmap = decoded
            analyzePhoto(decoded)
        }
    }

    val statusIndex = 1
    LaunchedEffect(scanResult, noFood, analyzeError, analyzing) {
        val uiState = when {
            analyzing -> "analyzing"
            analyzeError != null -> "error=$analyzeError"
            noFood -> "no_food"
            scanResult != null -> "success"
            else -> null
        }
        if (uiState != null) {
            Log.d(FoodScanCameraHandoff.LOG_TAG, "ui_state $uiState")
        }
        if (previewUri != null && (scanResult != null || noFood || analyzeError != null || analyzing)) {
            listState.scrollToItem(statusIndex)
        }
    }

    fun logScanFoods(edited: FoodScanResult) {
        if (logged) return
        if (edited.foods.isEmpty()) return
        CalorieStore.logScanResult(context, edited)
        scanResult = edited
        logged = true
        showMessage(loggedMessage)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            resetScanState()
            analyzing = true
            previewUri = uri
            showMessage(photoSelected)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = TakePictureWithUriGrants()
    ) { success ->
        val uri = pendingCameraUri
        val path = pendingCameraPath
        val immediateLength = photoContentLength(context, uri, path)
        val immediateExists = path != null && File(path).exists()
        Log.d(
            FoodScanCameraHandoff.LOG_TAG,
            "camera result success=$success uri=$uri length=$immediateLength"
        )
        Log.d(
            FoodScanCameraHandoff.LOG_TAG,
            "camera_callback_received success=$success uri=$uri path=$path"
        )
        Log.d(
            FoodScanCameraHandoff.LOG_TAG,
            "file_check exists=$immediateExists length=$immediateLength path=$path"
        )
        if (success) {
            resetScanState()
            analyzing = true
            previewUri = uri
            showMessage(photoCaptured)
            return@rememberLauncherForActivityResult
        }
        // Samsung often returns RESULT_CANCELED after OK; accept if the file has bytes.
        scope.launch {
            val length = withContext(Dispatchers.IO) {
                if (uri == null) -1L else waitForPhotoBytes(context, uri, path)
            }
            val exists = path != null && File(path).exists()
            Log.d(
                FoodScanCameraHandoff.LOG_TAG,
                "camera result after wait success=$success uri=$uri length=$length"
            )
            Log.d(
                FoodScanCameraHandoff.LOG_TAG,
                "file_check exists=$exists length=$length path=$path"
            )
            if (!FoodScanCameraHandoff.shouldAcceptCameraResult(false, length)) {
                Log.d(FoodScanCameraHandoff.LOG_TAG, "camera result ignored (cancel or empty)")
                return@launch
            }
            resetScanState()
            analyzing = true
            previewUri = uri
            showMessage(photoCaptured)
        }
    }

    fun launchCamera() {
        Log.d(FoodScanCameraHandoff.LOG_TAG, "take_photo_clicked")
        val app = context.applicationContext
        val cameraDir = File(app.cacheDir, "camera").apply { mkdirs() }
        val photoFile = File(cameraDir, "food_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            app,
            "${app.packageName}.fileprovider",
            photoFile
        )
        pendingCameraUri = uri
        pendingCameraPath = photoFile.absolutePath
        val exists = photoFile.exists()
        val length = if (exists) photoFile.length() else 0L
        Log.d(FoodScanCameraHandoff.LOG_TAG, "camera launch uri=$uri path=${photoFile.absolutePath}")
        Log.d(
            FoodScanCameraHandoff.LOG_TAG,
            "output_created uri=$uri path=${photoFile.absolutePath} exists=$exists length=$length"
        )
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

            val bitmap = previewBitmap
            val photoSession = previewUri != null || bitmap != null || analyzing || analyzeError != null
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (!photoSession) Arrangement.Center else Arrangement.Top
            ) {
                if (bitmap != null) {
                    item(key = "photo") {
                        val compact = analyzing || scanResult != null || noFood || analyzeError != null
                        Image(
                            bitmap = bitmap,
                            contentDescription = stringResource(R.string.scan_food_preview_description),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (compact) Modifier.height(140.dp)
                                    else Modifier.aspectRatio(4f / 3f)
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
                        )
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { clearPhoto() }) {
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
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (photoSession) {
                    item(key = "status") {
                        when {
                            analyzing -> FoodScanAnalyzingCard()
                            analyzeError != null -> FoodScanErrorCard(
                                message = analyzeError!!,
                                onRetry = { analyzePhoto() }
                            )
                            scanResult != null -> FoodScanResultCard(
                                result = scanResult!!,
                                logged = logged,
                                onLog = { logScanFoods(it) },
                                onScanAnother = { clearPhoto() }
                            )
                            noFood -> FoodScanNoFoodCard(onScanAnother = { clearPhoto() })
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                } else {
                    item(key = "empty") {
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
                    item(key = "camera") {
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
                    }
                    item(key = "gallery") {
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
                    }
                    item(key = "helper") {
                        Text(
                            text = stringResource(R.string.scan_food_helper),
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteAlpha30,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodScanAnalyzingCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NavySurface)
            .border(1.dp, GreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = GreenPrimary,
            strokeWidth = 3.dp
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.scan_food_analyzing),
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun FoodScanErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NavySurface)
            .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = White,
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = NavyDeep
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.scan_food_retry),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun FoodScanNoFoodCard(onScanAnother: () -> Unit) {
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
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onScanAnother,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
            border = BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.scan_food_scan_another),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/** Card showing the AI-estimated foods, editable nutrition, total and disclaimer. */
@Composable
private fun FoodScanResultCard(
    result: FoodScanResult,
    logged: Boolean,
    onLog: (FoodScanResult) -> Unit,
    onScanAnother: () -> Unit
) {
    var drafts by remember(result) {
        mutableStateOf(result.foods.map { FoodEditDraft.from(it) })
    }
    val parsedFoods = FoodScanEdit.parseAll(drafts)
    val canLog = !logged && parsedFoods != null && parsedFoods.isNotEmpty()
    val totalCalories = parsedFoods?.sumOf { it.calories } ?: result.foods.sumOf { it.calories }
    val totalProtein = parsedFoods?.sumOf { it.proteinG }
    val totalCarbs = parsedFoods?.sumOf { it.carbsG }
    val totalFat = parsedFoods?.sumOf { it.fatG }
    val multiple = result.foods.size > 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NavySurface)
            .border(1.dp, GreenPrimary.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.scan_food_result_heading),
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
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

        Spacer(Modifier.height(16.dp))

        if (logged) {
            result.foods.forEachIndexed { index, food ->
                if (index > 0) {
                    Spacer(Modifier.height(12.dp))
                    ScanCardDivider()
                    Spacer(Modifier.height(12.dp))
                }
                NutritionFactsBlock(
                    name = food.name,
                    caloriesValue = food.calories.toString(),
                    proteinValue = formatGrams(food.proteinG),
                    carbsValue = formatGrams(food.carbsG),
                    fatValue = formatGrams(food.fatG),
                    portionValue = food.estimatedPortion
                )
            }
        } else {
            drafts.forEachIndexed { index, draft ->
                if (index > 0) {
                    Spacer(Modifier.height(12.dp))
                    ScanCardDivider()
                    Spacer(Modifier.height(12.dp))
                }
                NutritionFactsBlock(
                    name = draft.name,
                    caloriesValue = draft.calories,
                    proteinValue = draft.proteinG,
                    carbsValue = draft.carbsG,
                    fatValue = draft.fatG,
                    portionValue = draft.estimatedPortion
                )
                Spacer(Modifier.height(12.dp))
                EditableFoodRow(
                    draft = draft,
                    onChange = { updated ->
                        drafts = drafts.toMutableList().also { it[index] = updated }
                    }
                )
            }
        }

        if (multiple) {
            Spacer(Modifier.height(16.dp))
            ScanCardDivider()
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.scan_food_meal_totals),
                color = GreenLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
            Text(
                text = stringResource(R.string.scan_food_kcal_value, totalCalories),
                color = GreenPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            if (totalProtein != null && totalCarbs != null && totalFat != null) {
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MacroColumn(
                        label = stringResource(R.string.scan_food_label_protein),
                        value = stringResource(R.string.scan_food_grams, formatGrams(totalProtein)),
                        modifier = Modifier.weight(1f)
                    )
                    MacroColumn(
                        label = stringResource(R.string.scan_food_label_carbs),
                        value = stringResource(R.string.scan_food_grams, formatGrams(totalCarbs)),
                        modifier = Modifier.weight(1f)
                    )
                    MacroColumn(
                        label = stringResource(R.string.scan_food_label_fat),
                        value = stringResource(R.string.scan_food_grams, formatGrams(totalFat)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (!logged && parsedFoods == null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.scan_food_edit_invalid),
                color = ErrorRed,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.scan_food_disclaimer),
            color = WhiteAlpha60,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                val edited = FoodScanEdit.resultFromDrafts(result, drafts) ?: return@Button
                onLog(edited)
            },
            enabled = canLog,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = NavyDeep,
                disabledContainerColor = GreenDim,
                disabledContentColor = WhiteAlpha60
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(if (logged) R.string.scan_food_logged else R.string.scan_food_log),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onScanAnother,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
            border = BorderStroke(1.dp, NavyBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.scan_food_scan_another),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ScanCardDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(NavyBorder)
    )
}

@Composable
private fun NutritionFactsBlock(
    name: String,
    caloriesValue: String,
    proteinValue: String,
    carbsValue: String,
    fatValue: String,
    portionValue: String
) {
    val caloriesInt = caloriesValue.trim().toIntOrNull()
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.scan_food_label_detected),
            color = GreenLight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
        )
        Text(
            text = name.ifBlank { "—" },
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.scan_food_label_calories),
            color = GreenLight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
        )
        Text(
            text = if (caloriesInt != null) {
                stringResource(R.string.scan_food_kcal_value, caloriesInt)
            } else {
                caloriesValue.ifBlank { "—" }
            },
            color = GreenPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp
        )

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            MacroColumn(
                label = stringResource(R.string.scan_food_label_protein),
                value = gramsOrRaw(proteinValue),
                modifier = Modifier.weight(1f)
            )
            MacroColumn(
                label = stringResource(R.string.scan_food_label_carbs),
                value = gramsOrRaw(carbsValue),
                modifier = Modifier.weight(1f)
            )
            MacroColumn(
                label = stringResource(R.string.scan_food_label_fat),
                value = gramsOrRaw(fatValue),
                modifier = Modifier.weight(1f)
            )
        }

        if (portionValue.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.scan_food_label_portion),
                color = GreenLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
            Text(
                text = portionValue,
                color = White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MacroColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = GreenLight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
        )
        Text(
            text = value,
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun gramsOrRaw(value: String): String {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return "—"
    val parsed = trimmed.toDoubleOrNull()
    return if (parsed != null) stringResource(R.string.scan_food_grams, formatGrams(parsed))
    else trimmed
}

@Composable
private fun EditableFoodRow(
    draft: FoodEditDraft,
    onChange: (FoodEditDraft) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = draft.name,
            onValueChange = { onChange(draft.copy(name = it)) },
            label = { Text(stringResource(R.string.scan_food_field_name), color = WhiteAlpha60) },
            singleLine = true,
            colors = novaScanFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = draft.estimatedPortion,
            onValueChange = { onChange(draft.copy(estimatedPortion = it)) },
            label = { Text(stringResource(R.string.scan_food_field_portion), color = WhiteAlpha60) },
            singleLine = true,
            colors = novaScanFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draft.calories,
                onValueChange = { onChange(draft.copy(calories = it)) },
                label = { Text(stringResource(R.string.scan_food_field_calories), color = WhiteAlpha60) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = novaScanFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = draft.proteinG,
                onValueChange = { onChange(draft.copy(proteinG = it)) },
                label = { Text(stringResource(R.string.scan_food_field_protein), color = WhiteAlpha60) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = novaScanFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draft.carbsG,
                onValueChange = { onChange(draft.copy(carbsG = it)) },
                label = { Text(stringResource(R.string.scan_food_field_carbs), color = WhiteAlpha60) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = novaScanFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = draft.fatG,
                onValueChange = { onChange(draft.copy(fatG = it)) },
                label = { Text(stringResource(R.string.scan_food_field_fat), color = WhiteAlpha60) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = novaScanFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun novaScanFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GreenPrimary,
    unfocusedBorderColor = NavyBorder,
    focusedTextColor = White,
    unfocusedTextColor = White,
    cursorColor = GreenPrimary,
    focusedContainerColor = NavyElevated,
    unfocusedContainerColor = NavyElevated
)

private fun formatGrams(v: Double): String =
    if (v == Math.floor(v) && !v.isInfinite()) v.toInt().toString()
    else v.toString().trimEnd('0').trimEnd('.')

/** Compress the already-downscaled preview bitmap to JPEG bytes for upload. */
private fun compressJpeg(bitmap: Bitmap, quality: Int = 85): ByteArray? = try {
    val software = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
    } else bitmap
    val out = ByteArrayOutputStream()
    if (software.compress(Bitmap.CompressFormat.JPEG, quality, out)) out.toByteArray() else null
} catch (e: Exception) {
    null
}

private fun decodePreviewBitmap(context: Context, uri: Uri, maxDim: Int = 1280): ImageBitmap? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
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
    Log.d(FoodScanCameraHandoff.LOG_TAG, "decode exception ${e.javaClass.simpleName}")
    Log.d(FoodScanCameraHandoff.LOG_TAG, "decode_failure reason=${e.javaClass.simpleName}")
    null
}

/**
 * TakePicture extra-output URI grants do not apply to extras unless the URI is
 * also in clipData. Samsung cameras often need read+write + clipData to write.
 */
private class TakePictureWithUriGrants : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return super.createIntent(context, input).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            clipData = ClipData.newRawUri("food-photo", input)
        }
    }
}

private fun photoContentLength(context: Context, uri: Uri?, path: String?): Long {
    if (uri == null) return -1L
    if (path != null) {
        val file = File(path)
        if (file.exists()) return file.length()
    }
    return try {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
    } catch (e: Exception) {
        -1L
    }
}

private suspend fun waitForPhotoBytes(
    context: Context,
    uri: Uri,
    path: String?,
    attempts: Int = 10,
    delayMs: Long = 100L
): Long {
    repeat(attempts) { i ->
        val length = photoContentLength(context, uri, path)
        if (length > 0L) return length
        if (i < attempts - 1) delay(delayMs)
    }
    return photoContentLength(context, uri, path)
}
