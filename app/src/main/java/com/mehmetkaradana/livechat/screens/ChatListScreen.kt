package com.mehmetkaradana.livechat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mehmetkaradana.livechat.CommonProgressBar
import com.mehmetkaradana.livechat.CommonRow
import com.mehmetkaradana.livechat.DestinationScreen
import com.mehmetkaradana.livechat.LcViewModel
import com.mehmetkaradana.livechat.TitleText
import com.mehmetkaradana.livechat.navigateTo

@Composable
fun ChatListScreen(navController: NavController, vm: LcViewModel) {

    val inProcess = vm.inProcessChats.value
    if (inProcess) {
        CommonProgressBar()
        //  BottomNavigationMenu(selectedItem = BottomNavigationItem.CHATLIST, navController =navController )
    } else {
        val chats = vm.chats.value
        val userData = vm.userData.value
        val showDialog = remember {
            mutableStateOf(false)
        }
        val onFabClick: () -> Unit = { showDialog.value = true }
        val onDismiss: () -> Unit = { showDialog.value = false }
        val onAddChat: (String) -> Unit = {
            vm.onAddChat(it)
            showDialog.value = false
        }

        Scaffold(floatingActionButton = {
            FAB(
                showDialog = showDialog.value,
                onFabClick = onFabClick,
                onDismiss = onDismiss,
                onAddChat = onAddChat
            )
        },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    TitleText(text = "Chats")
                    if(chats.isEmpty()){
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Text(text = "No Chats Available")
                        }
                    }else{
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(chats){chat->
                                val chatUser=
                                    if(chat.user1.userId==userData?.userId) chat.user2
                                else chat.user1

                                chat.chatId?.let{
                                    CommonRow(user = chatUser, onItemClick = {
                                        navigateTo(navController,DestinationScreen.SingleChat.createRoute(chatId = it))
                                    })
                                }

                            }
                        }
                    }
                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.CHATLIST,
                        navController = navController
                    )
                }
            })

    }


}


@Composable
fun FAB(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val addChatMember = remember {
        mutableStateOf("")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {//diyalog dışına veya geri tuşuna tıklandığında tetiklenir
                onDismiss.invoke()
                addChatMember.value = ""
            },
            confirmButton = {
                Button(onClick = { onAddChat(addChatMember.value) }) {
                    Text(text = "Add Chat")
                }
            },
            title = { Text(text = "Add Chat") },
            text = {
                OutlinedTextField(
                    value = addChatMember.value, onValueChange = {
                        addChatMember.value = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

            }

        )//alert dialog

    }
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = "", tint = Color.White)
    }
}