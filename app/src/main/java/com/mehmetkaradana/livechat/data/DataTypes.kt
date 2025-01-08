package com.mehmetkaradana.livechat.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.auth.User


data class UserData(
    var userId: String?="",
    var name: String?="",
    var number: String?="",
    var imageUrl: String?=""

){
    fun toMap()= mapOf(
        "userId" to userId,
        "name" to name,
        "number" to number,
        "imageUrl" to imageUrl
    )
}

/*
data class ChatUser (
    val userId:String?="",
    val name : String?="",
    val imageUrl: String?="",
    val number: String?=""
    )
*/
data class ChatData(
    val chatId : String?="",
    val user1 : UserData=UserData(),
    val user2 : UserData=UserData()

)

data class Message(
    var sendBy: String?="",
    val message:String?="",
   // val timestamp  :String?=""
    val timestamp: Any? = null
)

data class Status(
    val user : UserData=UserData(),
    val imageUrl : String?="",
    val timestamp: String?=null
)