package com.example.smarttaskai.ui.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.smarttaskai.ui.theme.PrimaryBlue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CameraScreen(
    mode: String,
    onPhotoCaptured: (Uri) -> Unit,
    onCloseCamera: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var reviewedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    // Launcher Akses Galeri HP
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) reviewedPhotoUri = uri
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aplikasi membutuhkan izin kamera", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Berikan Izin") }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onCloseCamera) { Text("Kembali", color = Color.Gray) }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (reviewedPhotoUri != null) {
            AsyncImage(
                model = reviewedPhotoUri,
                contentDescription = "Review Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { reviewedPhotoUri = null },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(Color.White)),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Retake")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retake")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { onPhotoCaptured(reviewedPhotoUri!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        if (mode == "smart") {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan AI")
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Use")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Photo")
                        }
                    }
                }
            }
        }

        else {
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = ContextCompat.getMainExecutor(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        imageCapture = ImageCapture.Builder().build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Use case binding failed", e)
                        }
                    }, executor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = onCloseCamera,
                modifier = Modifier.padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).align(Alignment.TopStart)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            Box(modifier = Modifier.align(Alignment.Center).size(250.dp).border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) {
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") }, // Buka galeri hanya untuk gambar
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 40.dp)
                        .size(50.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color.White)
                }

                Button(
                    onClick = {
                        takePhoto(context, imageCapture) { uri ->
                            reviewedPhotoUri = uri
                        }
                    },
                    modifier = Modifier.align(Alignment.Center).size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(4.dp).background(Color.White, CircleShape).border(3.dp, Color.LightGray, CircleShape))
                }
            }
        }
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture?, onPhotoCaptured: (Uri) -> Unit) {
    val imageCaptureUseCase = imageCapture ?: return
    val photoFile = File(context.cacheDir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCaptureUseCase.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) { Log.e("CameraX", "Photo capture failed", exc) }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onPhotoCaptured(Uri.fromFile(photoFile))
            }
        }
    )
}