package com.comp7506.mywardrobe.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.MyWardrobeApplication
import com.comp7506.mywardrobe.ui.viewmodel.AppViewModelFactory
import com.comp7506.mywardrobe.ui.viewmodel.PortraitCaptureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PortraitCaptureScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as MyWardrobeApplication
    val factory = remember { AppViewModelFactory(app, app.repository, app.locationProvider, app.weatherRepository) }
    val viewModel: PortraitCaptureViewModel = viewModel(factory = factory)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val galleryThumbnails by viewModel.galleryThumbnails.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var hasCameraError by remember { mutableStateOf(false) }
    var isGalleryExpanded by remember { mutableStateOf(false) }

    val galleryHeight by animateDpAsState(
        targetValue = if (isGalleryExpanded) (configuration.screenHeightDp.dp * 0.45f) else (configuration.screenHeightDp.dp * 0.25f),
        label = "galleryHeight"
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasCameraPermission = context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasCameraPermission) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            hasCameraPermission = true
        }
        viewModel.loadGalleryImages(context)
        try {
            cameraProvider = ProcessCameraProvider.getInstance(context).get()
        } catch (e: Exception) {
            hasCameraError = true
            e.printStackTrace()
        }
    }

    LaunchedEffect(cameraProvider, previewView) {
        if (cameraProvider != null && previewView != null && hasCameraPermission) {
            try {
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView!!.surfaceProvider)
                imageCapture = ImageCapture.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider!!.unbindAll()
                cameraProvider!!.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                hasCameraError = true
                e.printStackTrace()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraError) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera Error",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unable to initialize camera. Please check permissions and try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigateUp() }) {
                    Text("Go Back")
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(0.75f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewView = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    TextButton(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                navController.previousBackStackEntry?.savedStateHandle?.set("selectedPortraitUri", uri)
                                navController.popBackStack()
                            }
                        },
                        enabled = selectedImageUri != null,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Text("Next", style = MaterialTheme.typography.titleMedium)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 40.dp)
                    ) {
                        IconButton(
                            onClick = {
                                imageCapture?.let { capture ->
                                    coroutineScope.launch {
                                        try {
                                            val photoFile = createImageFile(context)
                                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                            capture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                    output.savedUri?.let { uri ->
                                                        viewModel.selectImage(uri.toString())
                                                        isGalleryExpanded = false
                                                    }
                                                }
                                                override fun onError(e: ImageCaptureException) {
                                                    Toast.makeText(context, "Capture failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Capture failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Camera,
                                contentDescription = "Capture",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp, 8.dp)
                    ) {
                        Text(
                            "please capture a full-body photo",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(galleryHeight)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.7f))
                            )
                        )
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    val dragAmountY = if (isGalleryExpanded) -1f else 1f
                                    isGalleryExpanded = dragAmountY < 0
                                }
                            ) { _, dragAmount ->
                                val threshold = 50f
                                if (dragAmount < -threshold) {
                                    isGalleryExpanded = true
                                } else if (dragAmount > threshold) {
                                    isGalleryExpanded = false
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Recent Photos",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(galleryThumbnails) { uri ->
                                val isSelected = uri == selectedImageUri
                                AsyncImage(
                                    model = Uri.parse(uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.selectImage(uri)
                                            isGalleryExpanded = false
                                        }
                                        .then(
                                            if (isSelected) {
                                                Modifier.padding(2.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF6200EE))
                                            } else {
                                                Modifier
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun createImageFile(context: Context): File = withContext(Dispatchers.IO) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val storageDir = File(context.getExternalFilesDir(null), "Pictures")
    if (!storageDir.exists()) storageDir.mkdirs()
    File(storageDir, "IMG_${timeStamp}.jpg")
}
