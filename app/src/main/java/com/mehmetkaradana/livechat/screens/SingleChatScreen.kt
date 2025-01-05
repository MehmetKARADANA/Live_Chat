package com.mehmetkaradana.livechat.screens

import android.text.style.BackgroundColorSpan
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.annotations.concurrent.Background
import com.mehmetkaradana.livechat.CommonDivider
import com.mehmetkaradana.livechat.CommonImage
import com.mehmetkaradana.livechat.DestinationScreen
import com.mehmetkaradana.livechat.LcViewModel
import com.mehmetkaradana.livechat.data.ChatUser
import com.mehmetkaradana.livechat.data.Message
import com.mehmetkaradana.livechat.data.UserData
import com.mehmetkaradana.livechat.navigateTo

@Composable
fun SingleChatScreen(navController: NavController, vm: LcViewModel, chatId: String) {

    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val onSendReply = {
        vm.onSendReply(chatId, reply)
        reply = ""
    }

    val myUser = vm.userData.value
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser = if (currentChat.user1.userId == myUser?.userId) currentChat.user2
    else currentChat.user1

    //mainthread bloklanmadan çalışır
    LaunchedEffect(key1 = Unit) {//Compose tarafından sunulan bir asenkron çalıştırma aracıdır
        vm.populateMessages(chatId)
    }

    BackHandler {
        navController.popBackStack()
        vm.depopulateMessages()
    }

    Column {
        Row {
            ChatHeader(chatUser = chatUser, onBackClicked = {
                // navigateTo(navController, DestinationScreen.ChatList.route)
                navController.popBackStack()
                vm.depopulateMessages()
            })

        }
        CommonDivider()
      /*  Row(modifier = Modifier.weight(1f)) {
            Text(text = vm.chatMessages.value.toString())
        }*/
        MessageBox(modifier = Modifier.weight(1f), chatMessages = vm.chatMessages.value, currentId =myUser?.userId?:"" )
        Row {
            ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReply = onSendReply)
        }
    }


}

@Composable
fun MessageBox(modifier: Modifier, chatMessages: List<Message>, currentId: String) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) {message->
            val aligment=if(message.sendBy==currentId) Alignment.End else Alignment.Start
            val color=if(message.sendBy==currentId)  Color(0xFF68C400) else Color(0xFFE0E0E0)
            Column(modifier= Modifier
                .fillMaxWidth()
                .padding(8.dp), horizontalAlignment = aligment) {
                Text(text = message.message.toString(), modifier=Modifier.background(color=color,
                    shape = CircleShape)
                    .padding(8.dp),
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun ChatHeader(chatUser: ChatUser, onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.ArrowBack,
            contentDescription = "",
            modifier = Modifier.clickable { onBackClicked.invoke() })
        CommonImage(
            data = chatUser.imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(
            text = chatUser.name ?: "---",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

    }
}

@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(value = reply, onValueChange = onReplyChange, maxLines = 3)
            Button(onClick = onSendReply) {
                Text(text = "Send")
            }
        }
    }
}