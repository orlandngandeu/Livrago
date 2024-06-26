package com.application.livrago.ui

import com.application.livrago.ui.theme.LivragoTheme
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.application.livrago.domain.notifications.NotificationService
import com.application.livrago.ui.composables.CarteBancaire
import com.application.livrago.ui.composables.CourseLivraison
import com.application.livrago.ui.composables.Liste
import com.application.livrago.ui.composables.LoginScreen
import com.application.livrago.ui.composables.MobileMoney
import com.application.livrago.ui.composables.MotoTaxi
import com.application.livrago.ui.composables.OrangeMoney
import com.application.livrago.ui.composables.Payment
import com.application.livrago.ui.composables.RegisterScreen
import com.application.livrago.ui.composables.Routes
import com.application.livrago.ui.composables.Type_course
import com.application.livrago.ui.screen.BlogDetailsScreen
import com.application.livrago.ui.screen.Dashboard
import com.application.livrago.ui.screen.HomeScreen
import com.application.livrago.ui.screen.SignInScreen
import com.application.livrago.ui.screen.SplashScreen
import com.application.livrago.ui.screen.UpdateBlogScreen
import com.application.livrago.ui.screen.personnal.Empreinte
import com.application.livrago.ui.screen.personnal.UpdateProfil
import com.application.livrago.util.GoogleAuthUiHelper
import com.application.myapp.Compte_Profil
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var googleAuthUiHelper: GoogleAuthUiHelper
    @Inject lateinit var oneTapClient : SignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LivragoTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()
                    val viewModel : MainViewModel = hiltViewModel()
                    val uiState = viewModel.uiState.value

                    NavHost(navController = navController,
                        startDestination = "splash_screen")
                    {

                        composable(
                            route = Routes.loginScreen
                        ){
                            LoginScreen(navController = navController)
                        }

                        composable("Dashboard")
                        {
                            Dashboard(
                                navController = navController,
                                currentUser = uiState.currentUser,
                                onSignOut = {
                                    //viewModel.signOut(oneTapClient)
                                    navController.navigate("compte_utilisateur")
                                },
                            )
                        }


                        composable(
                            route = Routes.registerScreen
                        ){
                            RegisterScreen(navController = navController)
                        }

                        composable(route = "MobileMoney"){
                            MobileMoney(navController = navController)
                        }

                        composable(route = "OrangeMoney"){
                            OrangeMoney(navController = navController)
                        }

                        composable(route = "carteBancaire"){
                            CarteBancaire(navController = navController)
                        }

                        composable(route = "payements"){
                            Payment(navController = navController)
                        }

                        composable(
                            route = "finger_settings"
                        ){
                            Empreinte(navController = navController)
                        }

                        composable(
                            route = "moto_taxi"
                        ){
                            MotoTaxi(navController = navController)
                        }

                        composable(
                            route = "course_livraison"
                        ){
                            CourseLivraison(navController = navController)
                        }

                        composable(
                            route = Routes.listeScreen
                        ){
                            Liste(navController = navController)
                        }


                        composable("update_profil"){
                            UpdateProfil(
                                navController  =  navController,
                                currentUser = uiState.currentUser,
                            )
                        }

                        composable("notification"){
                            NotificationService()
                        }

                        composable("type_course"){
                            Type_course(navController  =  navController)
                        }


                        //page de demarrage principal de livrago
                        composable("splash_screen"){
                            SplashScreen(navController  =  navController)
                        }

                        composable("compte_utilisateur"){
                            Compte_Profil(
                                navController  =  navController,
                                currentUser = uiState.currentUser,
                                onSignOut = {
                                    viewModel.signOut(oneTapClient)
                                },
                                )
                        }


                        //page de connexion ici
                        composable("signin"){

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = {result->
                                    if(result.resultCode == RESULT_OK){
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiHelper.getSignInResultFromIntentAndSignIn(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = Unit){
                                if(uiState.currentUser != null){
                                    navController.navigate("Dashboard")
                                }
                            }

                            LaunchedEffect(key1 = uiState.isSignInSuccessfull ){
                                if(uiState.isSignInSuccessfull){
                                    navController.navigate("Dashboard")
                                    viewModel.resetSignInState()
                                }
                            }

                            SignInScreen(
                                isLoading = uiState.isLoading,
                                currentUser = uiState.currentUser,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiHelper.actionSignIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }


                        //page homescreen
                        composable("home"){
                            HomeScreen(
                                currentUser = uiState.currentUser,
                                blogs = uiState.blogs,
                                isLoading = uiState.isLoading,
                                onSignOut = {
                                    //viewModel.signOut(oneTapClient)
                                    navController.navigate("compte_utilisateur")
                                },

                                onNavigateToBlogDetailsScreen = {blog ->
                                    val encodedUrl = URLEncoder.encode(blog.thumbnail,"UTF-8")
                                    navController.navigate(
                                        "blog_details?id=${blog.id}?title=${blog.title}" +
                                                "?content=${blog.content}" +
                                                "?username=${uiState.currentUser?.username}" +
                                                "?thumbnail=$encodedUrl"
                                    )
                                },

                                onNavigateToUpdateBlogScreen = {
                                    navController.navigate("blog_update")
                                },

                                onNavigateToSigninScreen = {
                                    navController.popBackStack(
                                        route = "signin", inclusive = false
                                    )
                                }
                            )
                        }



                        composable(
                            route = "blog_details?id={id}?title={title}?content={content}" +
                                    "?username={username}?thumbnail={thumbnail}",
                            arguments = listOf(
                                navArgument(name = "id", builder = {nullable = true}),
                                navArgument(name = "title", builder = {nullable = true}),
                                navArgument(name = "content", builder = {nullable = true}),
                                navArgument(name = "username", builder = {nullable = true}),
                                navArgument(name = "thumbnail", builder = {nullable = true}),
                            )
                        ){backStackEntry->
                            val id = backStackEntry.arguments?.getString("id")
                            val title = backStackEntry.arguments?.getString("title")
                            val content = backStackEntry.arguments?.getString("content")
                            val username = backStackEntry.arguments?.getString("username")
                            val thumbnail = backStackEntry.arguments?.getString("thumbnail")

                            val encededUrl = URLEncoder.encode(thumbnail, "UTF-8")

                            BlogDetailsScreen(
                                titleBlog = title,
                                contentBlog = content,
                                thumbnailBlog = thumbnail,
                                usernameBlog = username!!,
                                onBackPressed = { navController.popBackStack() },
                                onEditClicked = {
                                    navController.navigate(
                                        "blog_update?id=$id?title=$title" +
                                            "?content=$content?thumbnail=$encededUrl")
                                },
                                onDeleteClicked = {
                                    viewModel.deleteBlog(id!!)
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "blog_update?id={id}?title={title}?content={content}?thumbnail={thumbnail}",
                            arguments = listOf(
                                navArgument(name = "id", builder = {nullable = true}),
                                navArgument(name = "title", builder = {nullable = true}),
                                navArgument(name = "content", builder = {nullable = true}),
                                navArgument(name = "thumbnail", builder = {nullable = true}),
                            )
                        ){backStackEntry->
                            val id = backStackEntry.arguments?.getString("id")
                            val title = backStackEntry.arguments?.getString("title")
                            val content = backStackEntry.arguments?.getString("content")
                            val thumbnail = backStackEntry.arguments?.getString("thumbnail")

                            UpdateBlogScreen(
                                titleBlog = title,
                                contentBlog = content,
                                thumbnailBlog = thumbnail,
                                onBackPressed = { navController.popBackStack() },
                                onUpdateBlogCliked = {titleCb, contentCb, thumbnailCb->
                                    if(id==null){
                                        viewModel.onAddBlog(
                                            title = titleCb, content = contentCb,
                                            thumbnail = Uri.parse(thumbnailCb),
                                            user = uiState.currentUser!!
                                        )
                                    }else{
                                        viewModel.updateBlog(
                                            id = id, title = titleCb, content= contentCb,
                                            thumbnail = Uri.parse(thumbnailCb)
                                        )
                                    }
                                    navController.navigate("home"){
                                        popUpTo("home")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}