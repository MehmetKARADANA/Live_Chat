package com.mehmetkaradana.livechat.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mehmetkaradana.livechat.CheckSignedIn
import com.mehmetkaradana.livechat.CommonProgressBar
import com.mehmetkaradana.livechat.DestinationScreen
import com.mehmetkaradana.livechat.LcViewModel
import com.mehmetkaradana.livechat.R
import com.mehmetkaradana.livechat.navigateTo

@Composable
fun SignUpScreen(navController: NavController,vm : LcViewModel) {

    CheckSignedIn(vm = vm,navController= navController)

  Box (modifier = Modifier.fillMaxSize()){

      val focus= LocalFocusManager.current

      Column(modifier = Modifier
          .fillMaxSize()
          .wrapContentHeight()
          .verticalScroll(
              rememberScrollState()
          )
          .clickable {
              focus.clearFocus()
          },
          horizontalAlignment = Alignment.CenterHorizontally
      ) {

          val nameState = remember {
              mutableStateOf(TextFieldValue())
          }
          val numberState = remember {
              mutableStateOf(TextFieldValue())
          }
          val emailState = remember {
              mutableStateOf(TextFieldValue())
          }
          val passwordState = remember {
              mutableStateOf(TextFieldValue())
          }

          val focusColumn= LocalFocusManager.current


          Image(painter = painterResource(id = R.drawable.livechatapp),
              contentDescription ="LiveChat Icon",
              modifier = Modifier
                  .width(180.dp)
                  .padding(top = 16.dp)
                  .padding(8.dp))
          
          Text(text = "Signup", modifier = Modifier
              .padding(8.dp),
              fontFamily = FontFamily.SansSerif,
              fontWeight = FontWeight.Bold,
          )

          OutlinedTextField(value =nameState.value, modifier = Modifier.padding(8.dp)
              , onValueChange ={
              nameState.value=it
          },
              label = { Text(text = "Name")} )

          OutlinedTextField(value =numberState.value ,
              modifier = Modifier.padding(8.dp)
              , onValueChange ={
              numberState.value=it
          }, label = { Text(text = "Number")} )

          OutlinedTextField(value =emailState.value
              , modifier = Modifier.padding(8.dp), onValueChange ={
              emailState.value=it
          } , label = { Text(text = "E-Mail")})

          OutlinedTextField(value =passwordState.value,
              modifier = Modifier.padding(8.dp), onValueChange ={
              passwordState.value=it
          }, label = { Text(text = "Password")} )
          
          Button(onClick = {
              vm.signUp(
                  nameState.value.text,
                  numberState.value.text,
                  emailState.value.text,
                  passwordState.value.text
              )
          },
              modifier = Modifier.padding(8.dp)) {
              Text(text = " Sign Up")
          }
          
          Text(text = "Already a User ? Go to login ->",
              modifier = Modifier
                  .padding(8.dp)
                  .clickable {
                      navigateTo(navController, DestinationScreen.Login.route)
                  },
              color = Color.Black)

      }


  }
    if (vm.inProcess.value){
CommonProgressBar()
    }
}