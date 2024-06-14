package edu.put.ai_wardrobe.presentation.wardrobe
import edu.put.ai_wardrobe.ui.theme.CutePink


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import edu.put.ai_wardrobe.R
import edu.put.ai_wardrobe.components.BottomNavBar
//import edu.put.ai_wardrobe.presentation.home.placeholderAction
import edu.put.ai_wardrobe.utils.DateUtils

@Composable
fun WardrobeScreen(
    viewModel: WardrobeViewModel,
    navController: NavController
) {
    val cameraIcon: Painter = painterResource(R.drawable.camera_button)
    val filterIcon: Painter = painterResource(R.drawable.filter_button)
    val context: Context = LocalContext.current

    var hasCameraPermission by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    WardrobePage(
        navController = navController,
        cameraIcon = cameraIcon,
        filterIcon = filterIcon,
        viewModel = viewModel,
        hasCameraPermission = hasCameraPermission,
        context = context
    )
}
@Composable
private fun WardrobePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    cameraIcon: Painter,
    filterIcon: Painter,
    viewModel: WardrobeViewModel,
    hasCameraPermission: Boolean,
    context: Context
) {
    val clothesList by viewModel.clothesList.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showDialogDelete by remember { mutableStateOf(false) }
    var itemToDeleteUrl by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.background(Color.Transparent),
        bottomBar = {
            BottomNavBar(
                onHangerClick = { navController.navigate("wardrobe") },
                onPlannerClick = { navController.navigate("planner") },
                onHomeClick = { navController.navigate("home") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .paint(
                    painterResource(id = R.drawable.background_image),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.your_wardrobe),
                        contentDescription = "Your wardrobe",
                        modifier = Modifier.size(200.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentPadding = PaddingValues(2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(clothesList.chunked(2)) { _, rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { url ->
                                Column(
                                    modifier = Modifier
                                        .background(Color.Transparent),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    SubcomposeAsyncImage(model = url,
                                        contentDescription = "cloth",
                                        loading = { CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            color = CutePink) },
                                        modifier = Modifier.size(150.dp))

                                    Button(
                                        onClick = {
                                            showDialogDelete = true
                                            itemToDeleteUrl = url
                                        },
                                        shape = AbsoluteCutCornerShape(0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.bin),
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = "Delete"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDialogDelete) {
                DeleteConfirmationDialog(
                    onDismiss = { showDialogDelete = false },
                    onDeleteConfirmed = {
                        viewModel.deleteItemFromFirebase(itemToDeleteUrl)
                        showDialogDelete = false
                    }
                )
            }

            Button(
                onClick = {
                    showDialog = true
                },
                shape = AbsoluteCutCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
            ) {
                Image(
                    painter = filterIcon,
                    modifier = Modifier.size(48.dp),
                    contentDescription = "Filter"
                )
            }

            if (showDialog) {
                FilterDialog(onDismiss = { showDialog = false }) { selectedType ->
                    viewModel.filterClothes(selectedType)
                    showDialog = false
                }
            }

            FloatingActionButton(
                onClick = {
                    if (hasCameraPermission) {
                        Log.d("camera", "WardrobeScreen: going to camera")
                        navController.navigate("camera")
                    } else {
                        requestCameraPermission(context)
                    }
                },
                shape = AbsoluteCutCornerShape(0.dp),
                containerColor = Color.Transparent,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
            ) {
                Image(
                    painter = cameraIcon,
                    modifier = Modifier.size(48.dp),
                    contentDescription = "Camera"
                )
            }
        }
    }
}


@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Item") },
        text = { Text("Are you sure you want to delete this item?") },
        confirmButton = {
            TextButton(onClick = { onDeleteConfirmed() }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FilterDialog(onDismiss: () -> Unit, onFilterSelected: (String) -> Unit) {
    val clothTypes = listOf(
        "T-shirt", "Trouser", "Pullover", "Dress", "Coat",
        "Sandal", "Shirt", "Sneaker", "Bag", "Ankle Boot"
    )

    AlertDialog(
        containerColor = AlertDialogDefaults.containerColor,
        onDismissRequest = { onDismiss() },
        title = { Text("Filter Clothes") },
        text = {
            Column {
                clothTypes.forEach { type ->
                    TextButton(onClick = { onFilterSelected(type) } ) {
                        Text(type)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

private fun requestCameraPermission(context: Context) {
    ActivityCompat.requestPermissions(
        (context as Activity),
        arrayOf(Manifest.permission.CAMERA),
        REQUEST_CAMERA_PERMISSION_CODE
    )
}

private const val REQUEST_CAMERA_PERMISSION_CODE = 101
