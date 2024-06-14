package edu.put.ai_wardrobe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import edu.put.ai_wardrobe.R

@Composable
fun MainApp() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "background_image",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize())


    }
}
