package com.mehmetkaradana.livechat.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mehmetkaradana.livechat.utils.CommonDivider
import com.mehmetkaradana.livechat.utils.CommonImage
import com.mehmetkaradana.livechat.utils.CommonProgressBar
import com.mehmetkaradana.livechat.DestinationScreen
import com.mehmetkaradana.livechat.viewmodels.LcViewModel
import com.mehmetkaradana.livechat.utils.navigateTo
import com.mehmetkaradana.livechat.ui.components.BottomNavigationItem
import com.mehmetkaradana.livechat.ui.components.BottomNavigationMenu


@Composable
fun ProfileScreen(navController: NavController,vm : LcViewModel) {
    val inProcess=vm.inProcess.value

    if (inProcess){

            CommonProgressBar()

        }else{
             val userData=vm.userData.value

            var name by rememberSaveable {
                mutableStateOf(userData?.name?:"")
            }
            var number by rememberSaveable {
                mutableStateOf(userData?.number?:"")
            }
        Column (modifier= Modifier
            .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween){
           Row (modifier = Modifier.padding(8.dp)){
               ProfileContent(modifier = Modifier
                   .weight(1f)
                   .verticalScroll(rememberScrollState())
                   .padding(8.dp),
                   onBack = {
                       navigateTo(navController=navController,route=DestinationScreen.ChatList.route)
                   },
                   onSave = {
                       vm.createOrUpdateProfile(name=name,number=number)
                   },
                   vm=vm,
                   name = name,
                   number = number,
                   onNameChange = {name=it},
                   onNumberChange = {number=it},
                   onLogout={
                       vm.logout()
                       navigateTo(navController,DestinationScreen.Login.route)
                   }
               )
           }
           Row {
               BottomNavigationMenu(selectedItem = BottomNavigationItem.PROFILE, navController =navController )
           }
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(modifier: Modifier,
                   onBack : () ->Unit,
                   onSave : ()-> Unit,
                   vm: LcViewModel,
                   name : String,
                   number: String,
                   onNameChange : (String) ->Unit,
                   onNumberChange : (String) ->Unit,
                   onLogout :() ->Unit
){
    val imageUrl=vm.userData.value?.imageUrl
 //   Log.i("ProfileScreen :" ,"imageUrl"+imageUrl)//buraya null geliyor
Column {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        , horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "Back", modifier = Modifier.clickable {
            onBack.invoke()
        })
        Text(text = "Save", modifier = Modifier.clickable {
            onSave.invoke()
        })
    }

    CommonDivider()
    ProfileImage(imageUrl =imageUrl, vm=vm )
    CommonDivider()

    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp), verticalAlignment = Alignment.CenterVertically){

        Text(text = "Name ",modifier=Modifier.width(100.dp))
        TextField(value = name, onValueChange =onNameChange,
            colors = TextFieldDefaults.textFieldColors(
                focusedTextColor = Color.Black,
                containerColor = Color.Transparent
            )
        )

    }
    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp), verticalAlignment = Alignment.CenterVertically){
        Text(text = "Number ",modifier=Modifier.width(100.dp))
        TextField(value = number, onValueChange =onNumberChange,
            colors = TextFieldDefaults.textFieldColors(
                focusedTextColor = Color.Black,
                containerColor = Color.Transparent
            )
        )

    }
    CommonDivider()
    Row(modifier= Modifier
        .fillMaxWidth()
        .padding(16.dp),
        horizontalArrangement = Arrangement.Center) {

        Text(text = "LogOut",modifier=Modifier.clickable { onLogout.invoke() })

    }
}
}

@Composable
fun ProfileImage(imageUrl :String?,vm : LcViewModel){
//Log.i("Profile Image :","imageUrl "+imageUrl)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()) {uri ->
      //  Log.i("ProfileImage :","uri "+uri)
        uri?.let {
            vm.uploadProfileImage(uri)
        }
    }
    
    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)){
        Column(modifier= Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                launcher.launch("image/*")
            }, horizontalAlignment = Alignment.CenterHorizontally) {
            Card (shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)){
                CommonImage(data =imageUrl)
            }
            Text(text = "Change Profile Picture")
        }
        if(vm.inProcess.value){
            CommonProgressBar()
        }
    }

}