package com.mehmetkaradana.livechat.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mehmetkaradana.livechat.CommonDivider
import com.mehmetkaradana.livechat.CommonProgressBar
import com.mehmetkaradana.livechat.CommonRow
import com.mehmetkaradana.livechat.DestinationScreen
import com.mehmetkaradana.livechat.LcViewModel
import com.mehmetkaradana.livechat.TitleText
import com.mehmetkaradana.livechat.data.UserData
import com.mehmetkaradana.livechat.navigateTo

@Composable
fun StatusScreen(navController: NavController, vm: LcViewModel) {

    val inProcess = vm.inProgressStatus.value
    if (inProcess) {
        CommonProgressBar()
    } else {
        val statuses = vm.status.value
        val userData = vm.userData.value

        println("statuses :"+statuses)
        val myStatus = statuses.filter {
            it.user.userId == userData?.userId
        }
        println("mystatus :"+myStatus)

        val otherStatus = statuses.filter {
            it.user.userId != userData?.userId
        }

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    vm.uploadStatus(uri)
                }
            }

        Scaffold(floatingActionButton = {
            FAB {
                launcher.launch("image/*")
            }
        }, content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                TitleText(text = "Status")
                if (statuses.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "No Statuses avaliable")
                    }
                } else {
                    if (myStatus.isNotEmpty()) {
                        CommonRow(user = userData!!) {
                            navigateTo(
                                navController = navController,
                                route = DestinationScreen.SingleStatus.createRoute(userData.userId!!)
                            )
                        }
                    }
                    CommonDivider()
                    val uniqueUsers = otherStatus.map { it.user }.toSet().toList()
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(uniqueUsers) {
                            CommonRow(user = it) {
                                navigateTo(
                                    navController = navController,
                                    route = DestinationScreen.SingleStatus.createRoute(it.userId!!)
                                )
                            }
                        }
                    }
                    // }//
                }
                BottomNavigationMenu(selectedItem = BottomNavigationItem.STATUSLIST, navController)
            }
        })
    }

}


@Composable
fun FAB(onFabClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add Status",
            tint = Color.White
        )
    }
}