package edu.put.ai_wardrobe.presentation.planner

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.CalendarView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import edu.put.ai_wardrobe.R
import edu.put.ai_wardrobe.components.BottomNavBar
import edu.put.ai_wardrobe.ui.theme.CutePink

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel = viewModel(),
    navController: NavController

) {
    val clothes by viewModel.clothesList.collectAsState()
    val context = LocalContext.current


    var selectedTimestamp by remember { mutableStateOf<String?>(null) }
    var showAddOutfitScreen by remember { mutableStateOf(false) }
    var selectedClothes by remember { mutableStateOf<List<String>>(emptyList()) }

    val clothTypes = listOf(
        "T-shirt", "Trouser", "Pullover", "Dress", "Coat",
        "Sandal", "Shirt", "Sneaker", "Bag", "Ankle Boot"
    )

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            BottomNavBar(
                onHangerClick = { navController.navigate("wardrobe") },
                onPlannerClick = { navController.navigate("planner") },
                onHomeClick = { navController.navigate("home") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .paint(
                    painterResource(id = R.drawable.background_image),
                    contentScale = ContentScale.FillBounds
                )
        ) {

            Image(
                painter = painterResource(id = R.drawable.callendar),
                contentDescription = "Your wardrobe",
                modifier = Modifier.size(200.dp)
            )
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { CalendarView(context).apply{
                } },
                update = { calendarView ->
                    calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                        selectedTimestamp = "$year${String.format("%02d", month + 1)}${String.format("%02d", dayOfMonth)}"
                    }
                }
            )

            Button(
                onClick = {
                    if (selectedTimestamp != null) {
                        showAddOutfitScreen = true
                    }
                },
                modifier = Modifier.padding(vertical = 16.dp),
                shape = AbsoluteCutCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent),

            ) {
                Image(
                    painter = painterResource(id = R.drawable.add_button),
                    modifier = Modifier.size(48.dp),
                    contentDescription = "create"

                )
            }


        }
        if(showAddOutfitScreen){
            AddOutfitScreen(
                viewModel = viewModel,
                navController = navController,
                selectedTimestamp = selectedTimestamp ?: "",
                onDismiss = { showAddOutfitScreen = false }
            )
        }
    }
}



@Composable
fun ClothItem(url: String, isSelected: Boolean = false, onSelect: ((Boolean) -> Unit)? = null) {

    Row(verticalAlignment = Alignment.CenterVertically) {

        SubcomposeAsyncImage(model = url,
            contentDescription = "cloth",
            loading = { CircularProgressIndicator()},
            modifier = Modifier.size(150.dp))

        onSelect?.let {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { isSelected -> onSelect(isSelected) }
            )
        }
    }
}


