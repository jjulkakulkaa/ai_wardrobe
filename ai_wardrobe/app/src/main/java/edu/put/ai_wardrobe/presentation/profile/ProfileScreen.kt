package edu.put.ai_wardrobe.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import edu.put.ai_wardrobe.R
import edu.put.ai_wardrobe.components.BottomNavBar
import edu.put.ai_wardrobe.presentation.sign_in.UserData

@Composable
fun ProfileScreen(
    userData: UserData?,
    onSignOut: () -> Unit,
    navController: NavController
) {

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .paint(
                    painterResource(id = R.drawable.background_image),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userData?.profilePictureUrl != null) {
                    AsyncImage(
                        model = userData.profilePictureUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (userData?.username != null) {
                    Text(
                        text = userData.username,
                        textAlign = TextAlign.Center,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(onClick = onSignOut,
                    shape = AbsoluteCutCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent),
                    ){
                    Image(
                        painter = painterResource(id = R.drawable.logout),
                        modifier = Modifier.size(48.dp),
                        contentDescription = "signout"

                    )
                }
            }
        }
    }
}