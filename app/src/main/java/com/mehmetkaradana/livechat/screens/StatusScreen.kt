package com.mehmetkaradana.livechat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun StatusScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween) {
        Text(text = "Status Screen")
        BottomNavigationMenu(selectedItem = BottomNavigationItem.STATUSLIST, navController)
    }

}