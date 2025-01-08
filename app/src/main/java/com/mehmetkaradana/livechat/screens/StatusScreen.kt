package com.mehmetkaradana.livechat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.mehmetkaradana.livechat.CommonProgressBar
import com.mehmetkaradana.livechat.LcViewModel
import com.mehmetkaradana.livechat.TitleText

@Composable
fun StatusScreen(navController: NavController, vm: LcViewModel) {

    val inProcess = vm.inProgressStatus.value
    if (inProcess) {
        CommonProgressBar()
    } else {
        val statuses = vm.status.value
        val userData = vm.userData.value

        Scaffold(floatingActionButton = {
            FAB { }
        }, content = {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(it)) {
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