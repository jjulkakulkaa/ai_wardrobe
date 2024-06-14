package edu.put.ai_wardrobe.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import edu.put.ai_wardrobe.R

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    onHangerClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onHomeClick: () -> Unit,
    hangerIcon: Painter = painterResource(id = R.drawable.hanger_button),
    plannerIcon: Painter = painterResource(id = R.drawable.planner_button),
    homeIcon: Painter = painterResource(id = R.drawable.home_button)
) {
    BottomAppBar(
        modifier = modifier,
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onPlannerClick,
                    shape = AbsoluteCutCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,

                        ),

                ){
                    Image(
                        painter = plannerIcon,
                        modifier = Modifier.size(48.dp),
                        contentDescription = "planner"

                    )
                }
                Button(onClick = onHomeClick,
                    shape = AbsoluteCutCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,

                        ),

                ) {
                    Image(
                        painter = homeIcon,
                        modifier = Modifier.size(48.dp),
                        contentDescription = "home "
                    )
                }
                Button(onClick = onHangerClick,
                    shape = AbsoluteCutCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,

                        ),

                ) {
                    Image(
                        painter = hangerIcon,
                        modifier = Modifier.size(48.dp),
                        contentDescription = "wardrobe"
                    )
                }
            }
        }
    )
}