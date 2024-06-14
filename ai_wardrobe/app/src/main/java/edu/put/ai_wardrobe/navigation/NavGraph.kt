package edu.put.ai_wardrobe.navigation

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import edu.put.ai_wardrobe.presentation.camera.CameraPreviewScreen
import edu.put.ai_wardrobe.presentation.home.HomeScreen
import edu.put.ai_wardrobe.presentation.home.HomeViewModel
import edu.put.ai_wardrobe.presentation.planner.PlannerScreen
import edu.put.ai_wardrobe.presentation.planner.PlannerViewModel
import edu.put.ai_wardrobe.presentation.profile.ProfileScreen
import edu.put.ai_wardrobe.presentation.sign_in.GoogleAuthUiClient
import edu.put.ai_wardrobe.presentation.sign_in.SignInScreen
import edu.put.ai_wardrobe.presentation.sign_in.SignInViewModel
import edu.put.ai_wardrobe.presentation.wardrobe.WardrobeScreen
import edu.put.ai_wardrobe.presentation.wardrobe.WardrobeViewModel

import kotlinx.coroutines.launch


@Composable
fun NavGraph(
    navController: NavHostController,
    googleAuthUiClient: GoogleAuthUiClient,
    applicationContext: Context,
    lifecycleScope: LifecycleCoroutineScope
) {
    NavHost(navController = navController, startDestination = "sign_in") {

        composable("sign_in") {


            val viewModel = viewModel<SignInViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            LaunchedEffect(key1 = Unit) {
                if (googleAuthUiClient.getSignedInUser() != null) {
                    navController.navigate("home")
                }
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == ComponentActivity.RESULT_OK) {
                        lifecycleScope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(
                                intent = result.data ?: return@launch
                            )
                            viewModel.onSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(key1 = state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Sign in successful",
                        Toast.LENGTH_LONG
                    ).show()

                    navController.navigate("home")
                    viewModel.resetState()
                }
            }

            SignInScreen(
                state = state,
                onSignInClick = {
                    lifecycleScope.launch {
                        val signInIntentSender = googleAuthUiClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                signInIntentSender ?: return@launch
                            ).build()
                        )
                    }
                }
            )
        }

        composable("profile") {

            ProfileScreen(
                userData = googleAuthUiClient.getSignedInUser(),
                onSignOut = {
                    lifecycleScope.launch {
                        googleAuthUiClient.signOut()
                        Toast.makeText(
                            applicationContext,
                            "Signed out",
                            Toast.LENGTH_LONG
                        ).show()

                        navController.popBackStack()
                    }
                },
                navController = navController
            )
        }

        composable("home") {
            val viewModel = HomeViewModel(
                FirebaseStorage.getInstance(),
                userData = googleAuthUiClient.getSignedInUser())

            HomeScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable("wardrobe"){
            val viewModel = WardrobeViewModel(
                storage = FirebaseStorage.getInstance(),
                userData = googleAuthUiClient.getSignedInUser()
            )

            WardrobeScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable("planner"){
            val viewModel = PlannerViewModel(
                storage = FirebaseStorage.getInstance(),
                userData = googleAuthUiClient.getSignedInUser()
            )

            PlannerScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable("camera"){
            val userData = googleAuthUiClient.getSignedInUser()
            val viewModel = HomeViewModel(
                FirebaseStorage.getInstance(),
                userData = userData)

            CameraPreviewScreen(viewModel.getUserStorageRef(userData!!.userId))
        }

    }
}