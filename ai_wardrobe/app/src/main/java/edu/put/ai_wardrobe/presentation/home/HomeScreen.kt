package edu.put.ai_wardrobe.presentation.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.put.ai_wardrobe.R
import edu.put.ai_wardrobe.components.BottomNavBar
import edu.put.ai_wardrobe.ui.theme.CutePink
import edu.put.ai_wardrobe.utils.DateUtils
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val userId = viewModel.userId!!
    val storage by lazy { FirebaseStorage.getInstance() }
    val userStorageRef = viewModel.getUserStorageRef(userId)
    val selectedDate = viewModel.selectedDate.collectAsState()
    val imagesUrl = viewModel.imagesUrl.collectAsState()

    HomePage(
        userStorageRef = userStorageRef,
        viewModel = viewModel,
        selectedDate = selectedDate,
        imagesUrl = imagesUrl,
        navController = navController
    )
}

@Composable
private fun HomePage(
    modifier: Modifier = Modifier,
    userStorageRef: StorageReference,
    viewModel: HomeViewModel,
    selectedDate: State<LocalDate>,
    imagesUrl: State<List<String>>,
    navController: NavController
) {
    val rightArrowIcon: Painter = painterResource(R.drawable.right_arrow_button)
    val leftArrowIcon: Painter = painterResource(R.drawable.left_arrow_button)

    Scaffold(
        modifier = modifier,
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
            Column {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween){
                    Image(
                        painter = painterResource(id = R.drawable.outfits),
                        contentDescription = "Your wardrobe",
                        modifier = Modifier.size(200.dp)
                    )
                    Button(onClick = {
                        navController.navigate("profile")

                    },
                        shape = AbsoluteCutCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),) {
                        Image(
                            painter = painterResource(R.drawable.profile),
                            modifier = Modifier.size(48.dp),
                            contentDescription = "profile"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        viewModel.updatePageData(offset = -1, userStorageRef)
                    },
                        shape = AbsoluteCutCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),) {
                        Image(
                            painter = leftArrowIcon,
                            modifier = Modifier.size(48.dp),
                            contentDescription = "date left"
                        )
                    }
                    Column {
                        val (dayOfWeek, formattedDate) = DateUtils.formatDateForDisplay(selectedDate.value)
                        Text(text = dayOfWeek)
                        Text(text = formattedDate)
                    }
                    Button(onClick = {
                        viewModel.updatePageData(offset = 1, userStorageRef)
                        Log.d("global date", "HomePage: $selectedDate")
                    },
                        shape = AbsoluteCutCornerShape(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),) {
                        Image(
                            painter = rightArrowIcon,
                            modifier = Modifier.size(48.dp),
                            contentDescription = "date right"
                        )
                    }
                }


                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    items(imagesUrl.value) { url ->
                        SubcomposeAsyncImage(model = url,
                            contentDescription = "cloth",
                            loading = { CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                color = CutePink) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 30.dp, vertical = 8.dp),

                            )
                    }
                }
            }
        }
    }
}

