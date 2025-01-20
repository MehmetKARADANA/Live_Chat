package com.mehmetkaradana.livechat.viewmodels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.google.firebase.StartupTime
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.mehmetkaradana.livechat.data.CHATS
import com.mehmetkaradana.livechat.data.ChatData
import com.mehmetkaradana.livechat.data.Event
import com.mehmetkaradana.livechat.data.MESSAGE
import com.mehmetkaradana.livechat.data.Message
import com.mehmetkaradana.livechat.data.STATUS
import com.mehmetkaradana.livechat.data.Status
import com.mehmetkaradana.livechat.data.USER_NODE
import com.mehmetkaradana.livechat.data.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
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
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    var currentChatMessagesListener: ListenerRegistration? = null
    val inProgressChatMessages = mutableStateOf(false)

    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessages.value = true
        currentChatMessagesListener =
            db.collection(CHATS).document(chatId).collection(MESSAGE).orderBy("timestamp")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        handleException(error)
                    }
                    if (value != null) {
                        chatMessages.value = value.documents.mapNotNull {
                            it.toObject<Message>()
                        }
                    }
                    inProgressChatMessages.value = false//!!
                }
    }

    fun depopulateMessages() {
        chatMessages.value = listOf()
        currentChatMessagesListener = null
    }

    fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {//
                handleException(error)
            } else {
                if (value != null) {
                    chats.value = value.documents.mapNotNull {
                        it.toObject<ChatData>()
                    }
                    inProcessChats.value = false
                }
            }

        }


    }

    fun populatesStatus() {
        val timeDelta = 24L * 60 * 60

        val time = Timestamp.now()
        // Milisaniyeyi saniyeye çeviriyoruz
        val twentyFourHoursAgo = Timestamp(time.seconds-timeDelta, time.nanoseconds)

        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }

            if (value != null) {

                val currentConnections = arrayListOf(userData.value?.userId)

                val chats = value.toObjects<ChatData>()
                chats.forEach { chat ->
                    if (userData.value?.userId == chat.user1.userId) {
                        currentConnections.add(chat.user2.userId)
                    } else
                        currentConnections.add(chat.user1.userId)
                }

                db.collection(STATUS).whereIn("user.userId", currentConnections)/*.whereGreaterThan("timestamp",twentyFourHoursAgo)*/
                    .addSnapshotListener { value2, error2 ->
                        if (error2 != null) {
                            handleException(error)
                        }
                        if (value2 != null) {
                            status.value = value2.toObjects<Status>()
                            inProgressStatus.value = false
                        }
                    }

            }
            inProgressStatus.value=false

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
        currentChatMessagesListener = null
        eventMutableState.value = Event("Logged Out")
    }


    fun onSendReply(chatId: String, message: String) {
        //val time = Calendar.getInstance().time.toString()
        val time = FieldValue.serverTimestamp()

        val msg = Message(userData.value?.userId, message, timestamp = time)
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)
    }

    fun uploadProfileImage(uri: Uri) {

        uploadImage(uri) {
            createOrUpdateProfile(imageurl = it.toString())
            // Log.i("uploadProfileImage : ", "imageUrl" + it.toString())
        }

    }

    fun uploadStatus(uri: Uri) {

        uploadImage(uri) {//!! parametre olan uri contentpicker yanıtı onu almamalıyım
            createStatus(it.toString())
        }

        populatesStatus()

    }

    fun createStatus(imageurl: String) {
        val newStatus = Status(
            user = UserData(
                userId = userData.value?.userId,
                name = userData.value?.name,
                imageUrl = userData.value?.imageUrl,
                number = userData.value?.number
            ),
            imageUrl = imageurl, timestamp = FieldValue.serverTimestamp()
        )

        db.collection(STATUS).document().set(newStatus)
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
                    getUserData(uid)
                    updateChatUser(
                        uid = uid,
                        name = userData.name,
                        number = userData.number,
                        imageurl = userData.imageUrl
                    )
                    updateStatusUser(
                        uid = uid,
                        name = userData.name,
                        number = userData.number,
                        imageurl = userData.imageUrl
                    )
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

    private fun updateChatUser(
        uid: String?, name: String? = null,
        number: String? = null,
        imageurl: String? = null
    ) {
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", uid),
                Filter.equalTo("user2.userId", uid)
            )
        ).get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                handleException()
            } else {
                for (document in documents) {
                    val chat = document.toObject<ChatData>()

                    // Eşleşen kullanıcıyı kontrol et ve fonksiyonu uygula
                    if (chat.user1.userId == uid) {
                        val chatuser1 = UserData(
                            userId = uid,
                            name = name ?: chat.user1.name,
                            number = number ?: chat.user1.number,
                            imageUrl = imageurl ?: chat.user1.imageUrl
                        )
                        db.collection(CHATS)
                            .document(document.id)
                            .update("user1", chatuser1)
                            .addOnSuccessListener {
                                Log.i("ChatUserUpdate", "User1 bilgileri başarıyla güncellendi.")
                            }
                            .addOnFailureListener { e ->
                                /*   Log.d(
                                       "ChatUserUpdate",
                                       "User2 güncellenirken hata oluştu: ${e.message}"
                                   )*/
                            }

                    } else if (chat.user2.userId == uid) {
                        val chatuser2 = UserData(
                            userId = uid,
                            name = name ?: chat.user1.name,
                            number = number ?: chat.user1.number,
                            imageUrl = imageurl ?: chat.user1.imageUrl
                        )
                        db.collection(CHATS)
                            .document(document.id)
                            .update("user2", chatuser2)
                            .addOnSuccessListener {
                                Log.i("ChatUserUpdate", "User2 bilgileri başarıyla güncellendi.")
                            }
                            .addOnFailureListener { e ->
                                Log.d(
                                    "ChatUserUpdate",
                                    "User2 güncellenirken hata oluştu: ${e.message}"
                                )
                            }

                    }
                }
            }

        }.addOnFailureListener {
            handleException(it)
        }

    }

    private fun updateStatusUser(
        uid: String?, name: String? = null,
        number: String? = null,
        imageurl: String? = null
    ) {
        db.collection(STATUS).where(

                Filter.equalTo("user.userId", uid),

        ).get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                handleException()
            } else {
                for (document in documents) {
                    val status = document.toObject<Status>()

                    // Eşleşen kullanıcıyı kontrol et ve fonksiyonu uygula
                    if (status.user.userId == uid) {
                        val statusUser = UserData(
                            userId = uid,
                            name = name ?: status.user.name,
                            number = number ?: status.user.number,
                            imageUrl = imageurl ?: status.user.imageUrl
                        )
                        db.collection(STATUS)
                            .document(document.id)
                            .update("user", statusUser)
                            .addOnSuccessListener {
                                Log.i("StatusUserUpdate", "User bilgileri başarıyla güncellendi.")
                            }
                            .addOnFailureListener { e ->
                                Log.d(
                                    "StatusUserUpdate",
                                    "User güncellenirken hata oluştu: ${e.message}"
                                )
                            }

                    }

                }
            }

        }.addOnFailureListener {
            handleException(it)
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
                populatesStatus()
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
                                val id = db.collection(CHATS)
                                    .document().id//Firestore tarafından otomatik olarak oluşturulan benzersiz
                                val chat = ChatData(
                                    chatId = id,
                                    UserData(
                                        userId = userData.value?.userId,
                                        name = userData.value?.name,
                                        imageUrl = userData.value?.imageUrl,
                                        number = userData.value?.number
                                    ),
                                    UserData(
                                        userId = chatPartner.userId,
                                        name = chatPartner.name,
                                        imageUrl = chatPartner.imageUrl,
                                        number = chatPartner.number
                                    )
                                )

                                db.collection(CHATS).document(id).set(chat)

                            }
                        }.addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chat already exists")
                }
            }
        }
    }


}