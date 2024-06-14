package edu.put.ai_wardrobe.presentation.planner

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.put.ai_wardrobe.R

@Composable
fun AddOutfitScreen(
    viewModel: PlannerViewModel,
    navController: NavController,
    selectedTimestamp: String,
    onDismiss: () -> Unit,

) {
    val clothTypes = listOf(
        "T-shirt", "Trouser", "Pullover", "Dress", "Coat",
        "Sandal", "Shirt", "Sneaker", "Bag", "Ankle Boot"
    )

    val clothes by viewModel.clothesList.collectAsState()
    var selectedClothes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedClothType by remember { mutableStateOf(clothTypes.first()) }
    var filterExpanded by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.background_image),
                contentScale = ContentScale.FillBounds
            )
    ){
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)) {
            // Filter button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(48.dp)
            ) {
                Button(onClick = { filterExpanded = !filterExpanded },
                    shape = AbsoluteCutCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),) {
                    Icon(painter = painterResource(R.drawable.filter_button),
                        contentDescription = "Filter",
                        modifier = Modifier.size(48.dp),)
                }
                DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                    clothTypes.forEach { type ->
                        DropdownMenuItem(onClick = {
                            selectedClothType = type
                            viewModel.fetchClothes(viewModel.userId!!, selectedClothType)
                            filterExpanded = false
                        },
                            text = {
                                Text(text = type)
                            })
                    }
                }
            }

            // Clothes grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f)
            ) {
                items(clothes) { url ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ClothItem(url, selectedClothes.contains(url)) { isSelected ->
                            selectedClothes = if (isSelected) {
                                selectedClothes + url
                            } else {
                                selectedClothes - url
                            }
                        }
                    }
                }
            }
            // Confirm button
            Button(
                onClick = {
                    viewModel.uploadUserOutfit(viewModel.userOutfitsRef, selectedClothes, selectedTimestamp)
                    navController.navigateUp()
                },
                shape = AbsoluteCutCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(4.dp)
            ) {
                Image(
                    painter =  painterResource(id = R.drawable.add_button),
                    modifier = Modifier.size(48.dp),
                    contentDescription = "signin"

                )
            }
            Spacer(modifier = Modifier.height(65.dp))
        }

    }

}
