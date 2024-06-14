package edu.put.ai_wardrobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import edu.put.ai_wardrobe.navigation.NavGraph
import edu.put.ai_wardrobe.presentation.sign_in.GoogleAuthUiClient
import edu.put.ai_wardrobe.ui.theme.Ai_wardrobeTheme

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            Ai_wardrobeTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ) {
                    MainApp()
                    val navController = rememberNavController()
                    NavGraph(navController = navController, googleAuthUiClient = googleAuthUiClient, applicationContext = applicationContext, lifecycleScope = lifecycleScope)
                }

            }


        }
    }
}

