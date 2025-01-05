package com.mehmetkaradana.livechat

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.google.firestore.v1.DocumentTransform
import com.mehmetkaradana.livechat.data.CHATS
import com.mehmetkaradana.livechat.data.ChatData
import com.mehmetkaradana.livechat.data.ChatUser
import com.mehmetkaradana.livechat.data.Event
import com.mehmetkaradana.livechat.data.MESSAGE
import com.mehmetkaradana.livechat.data.Message
import com.mehmetkaradana.livechat.data.USER_NODE
import com.mehmetkaradana.livechat.data.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class LcViewModel @Inject constructor(

    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage

) : ViewModel() {

    val inProcess = mutableStateOf(false)
    val inProcessChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)//??
    var signIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages= mutableStateOf<List<Message>>(listOf())
    var currentChatMessagesListener : ListenerRegistration?=null
    val inProgressChatMessages = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessages(chatId: String){
        inProgressChatMessages.value=true
        currentChatMessagesListener=db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener{ value,error->
            if(error != null){
                handleException(error)
            }
            if(value !=null ){
                chatMessages.value=value.documents.mapNotNull {
                    it.toObject<Message>()
                }.sortedBy { it.timestamp }
            }
                inProgressChatMessages.value=false//!!
        }
    }

    fun depopulateMessages(){
        chatMessages.value= listOf()
        currentChatMessagesListener=null
    }

    fun populateChats(){
        inProcessChats.value=true
        db.collection(CHATS).where(Filter.or(
            Filter.equalTo("user1.userId",userData.value?.userId),
            Filter.equalTo("user2.userId",userData.value?.userId)
        )).addSnapshotListener{value,error ->
            if(error!=null){//
                handleException(error)
            }else{
                if(value!=null){
                    chats.value=value.documents.mapNotNull {
                        it.toObject<ChatData>()
                    }
                    inProcessChats.value=false
                }
            }

        }


    }
    fun signUp(name: String, number: String, email: String, password: String) {
        inProcess.value = true

        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = " Please Fill All fields")
            return
        }


        inProcess.value = true
        db.collection(USER_NODE).whereEqualTo("number", number).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                    if (it.isSuccessful) {
                        signIn.value = true
                        Log.d("TAG", "signUp: User Logged In")
                        createOrUpdateProfile(name, number)
                    } else {
                        handleException(it.exception, customMessage = "Sign Up failed")
                    }
                }
            } else {
                handleException(customMessage = " number Already Exist")
                inProcess.value = false
            }
        }


    }

    fun loginIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill the all Fields")
            return
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProcess.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(exception = it.exception, customMessage = "Login Failed")
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulateMessages()
        currentChatMessagesListener=null
        eventMutableState.value = Event("Logged Out")
    }


    fun onSendReply(chatId : String,message: String){
        //val time = Calendar.getInstance().time.toString()
        val time = FieldValue.serverTimestamp().toString()
        val msg= Message(userData.value?.userId,message,time)
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)
    }

    fun uploadProfileImage(uri: Uri) {

        uploadImage(uri) {
            createOrUpdateProfile(imageurl = it.toString())
            Log.i("uploadProfileImage : ", "imageUrl" + it.toString())
        }

    }


    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        //  Log.i("UploadImage 1", "uri112"+uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            // Log.i("UploadImage","result :"+result)
            result?.addOnSuccessListener(onSuccess)
            inProcess.value = false
        }.addOnFailureListener {
            handleException(it)
            //inProcess.value=false
        }

    }


    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageurl: String? = null
    ) {
        //  inProcess.value=true
        var uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageurl ?: userData.value?.imageUrl

        )


        uid?.let {
            inProcess.value = true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    db.collection(USER_NODE).document(uid).update(userData.toMap())
                    inProcess.value = false
                } else {
                    db.collection(USER_NODE).document(uid).set(userData)
                    inProcess.value = false
                    getUserData(uid)
                }
            }.addOnFailureListener {
                handleException(it, "Cannot Retrieve User")
            }
        }
    }

    private fun getUserData(uid: String) {
        inProcess.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->

            if (error != null) {
                handleException(error, "Can not Retrieve User")
                inProcess.value = false
            }
            if (value != null) {
                var user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
                populateChats()
            }

        }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("LiveChatApp", "live chat exception: ", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMsg else customMessage
        eventMutableState.value = Event(message)
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly()) {
            handleException(customMessage = "Number must be contain digits only")
        } else {
            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ),
                    Filter.and(
                        Filter.equalTo("user2.number", number),
                        Filter.equalTo("user1.number", userData.value?.number)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(USER_NODE).whereEqualTo("number", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(customMessage = "number not found")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id//Firestore tarafından otomatik olarak oluşturulan benzersiz
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.imageUrl,
                                        userData.value?.number
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.imageUrl,
                                        chatPartner.number
                                    )
                                )

                                db.collection(CHATS).document(id).set(chat)

                            }
                        }.addOnFailureListener{
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chat already exists")
                }
            }
        }
    }


}