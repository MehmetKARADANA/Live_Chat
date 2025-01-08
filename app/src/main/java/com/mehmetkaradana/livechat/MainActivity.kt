package com.mehmetkaradana.livechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mehmetkaradana.livechat.screens.ChatListScreen
import com.mehmetkaradana.livechat.screens.LoginScreen
import com.mehmetkaradana.livechat.screens.ProfileScreen
import com.mehmetkaradana.livechat.screens.SignUpScreen
import com.mehmetkaradana.livechat.screens.SingleChatScreen
import com.mehmetkaradana.livechat.screens.SingleStatusScreen
import com.mehmetkaradana.livechat.screens.StatusScreen
import com.mehmetkaradana.livechat.ui.theme.LiveChatTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.processor.internal.definecomponent.codegen._dagger_hilt_android_components_ViewWithFragmentComponent


sealed class DestinationScreen(var route: String){
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}"){
        fun createRoute(chatId : String) = "singleChat/$chatId"
    }

    object StatusList : DestinationScreen("statusList")
    object SingleStatus  :DestinationScreen("singleStatus/{userId}"){
        fun createRoute(userId : String) = "singleStatus/$userId"
    }


}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // enableEdgeToEdge()
        setContent {
            LiveChatTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    ChatAppNavigation()
                }
            }
        }
    }

@Composable
fun ChatAppNavigation(){
    val navController = rememberNavController()
    var vm= hiltViewModel<LcViewModel>()

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {

        composable(DestinationScreen.SignUp.route) {
            SignUpScreen(navController,vm)
        }

        composable(DestinationScreen.Login.route) {
            LoginScreen(vm,navController)
        }

        composable(DestinationScreen.ChatList.route) {
            ChatListScreen(navController,vm)
        }
        composable(DestinationScreen.SingleChat.route) {
            val chatId=it.arguments?.getString("chatId")
            chatId?.let {
                SingleChatScreen(navController,vm,chatId)
            }

        }
        composable(DestinationScreen.StatusList.route) {
            StatusScreen(navController,vm)
        }
        composable(DestinationScreen.SingleStatus.route) {
            SingleStatusScreen()
        }

        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController,vm)
        }



    }

}
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LiveChatTheme {

    }
}