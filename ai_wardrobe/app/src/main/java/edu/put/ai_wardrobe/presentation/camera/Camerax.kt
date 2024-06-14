package edu.put.ai_wardrobe.presentation.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.firebase.storage.StorageReference
import edu.put.ai_wardrobe.R
import edu.put.ai_wardrobe.domain.ClothesRecognizer
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine




@Composable
fun CameraPreviewScreen(
    storageRef: StorageReference
) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val clothesRecognizer = ClothesRecognizer(context = context)

    // State to manage the confirmation dialog visibility
    val showConfirmationDialog = remember { mutableStateOf(false) }
    val capturedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val detectedClothType = remember { mutableStateOf("") }
    val imageName = remember { mutableStateOf("") }

    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    // Function to handle image capture result
    val onImageCaptured: () -> Unit = {
        showConfirmationDialog.value = true
    }

    // Launched effect to bind camera preview to lifecycle
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // Box composable to display camera preview and button
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // Button to capture image
        Button(onClick = {
            captureImage(imageCapture, context, clothesRecognizer) { bitmap, clothType, name ->
                capturedBitmap.value = bitmap
                detectedClothType.value = clothType
                imageName.value = name
                onImageCaptured()
            }
        },
            shape = AbsoluteCutCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent),) {
            Image(
                painter = painterResource(id = R.drawable.camera_button),
                modifier = Modifier.size(48.dp),
                contentDescription = "create"

            )
        }

        // Confirmation dialog
        if (showConfirmationDialog.value && capturedBitmap.value != null) {
            ConfirmationDialog(
                bitmap = capturedBitmap.value!!,
                clothType = detectedClothType.value,
                onConfirm = {selectedClothType ->
                    showConfirmationDialog.value = false

                    // Upload to Firebase
                    capturedBitmap.value?.let { bitmap ->
                        uploadToFirebase(bitmap, storageRef, imageName.value, selectedClothType)
                    }
                },

                onDismiss = { showConfirmationDialog.value = false }
            )
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun captureImage(
    imageCapture: ImageCapture,
    context: Context,
    clothesRecognizer: ClothesRecognizer,
    onImageCaptured: (Bitmap, String, String) -> Unit
) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val name = "$timestamp.jpeg"
    Log.d("CaptureImage", "captureImage: {$name}")

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
    }
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri
                Log.d("CaptureImage", "Image saved to: $savedUri")
                if (savedUri != null) {
                    val inputStream = context.contentResolver.openInputStream(savedUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    Log.d("Capture Image", "onImageSaved: IMG NOT NULL SO SAVING")

                    val clothType = clothesRecognizer.classifyImage(bitmap)

                    onImageCaptured(bitmap, clothType, name)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CaptureImage", "Capture failed: $exception")
            }
        }
    )
}

fun uploadToFirebase(bitmap: Bitmap, storageRef: StorageReference, imageName: String, clothType: String) {
    Log.d("Firebase upload", "uploadToFirebase: Img uploading to {$storageRef} ")
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    val data = baos.toByteArray()

    val imageRef = storageRef.child("clothes/$clothType/$imageName")
    Log.d("Firebase upload", "uploadToFirebase: {$imageRef}")
    // Upload image data to Firebase Storage
    val uploadTask = imageRef.putBytes(data)
    uploadTask.addOnSuccessListener { taskSnapshot ->
        Log.d("uploadToFirebase", "Image uploaded successfully: ${taskSnapshot.metadata?.path}")
    }.addOnFailureListener { exception ->
        Log.e("uploadToFirebase", "Failed to upload image: $exception")
    }
}



