package edu.put.ai_wardrobe.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import edu.put.ai_wardrobe.presentation.sign_in.UserData
import edu.put.ai_wardrobe.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class HomeViewModel(
    private val storage: FirebaseStorage,
    userData: UserData?) : ViewModel() {

    val userId: String? = userData?.userId

    private var _selectedDate =  MutableStateFlow(LocalDate.now())
    var selectedDate = _selectedDate.asStateFlow()

    private var _imagesUrl = MutableStateFlow<List<String>>(emptyList())
    val imagesUrl = _imagesUrl.asStateFlow()

    val realtimeDatabase = Firebase.database("https://aiwardrobe-a3502-default-rtdb.firebaseio.com/")

    private var _userOutfitsRef: StorageReference? = null
    val userOutfitsRef: StorageReference?
        get() = _userOutfitsRef

    init {
        userId?.let {
            _userOutfitsRef = storage.reference.child("users").child(it).child("outfits")
        }
    }
    fun getUserStorageRef(userId: String): StorageReference {

        val filePath = "users/$userId/userdata.txt"
        val storageRef = storage.reference.child(filePath)

        // jak jest user to nie bedzie erroru jak nie ma to error
        storageRef.metadata.addOnSuccessListener { metadata ->
            Log.d("Storage", "Old User: $filePath")

        }.addOnFailureListener { e ->
            // nie ma takiego usera w storage
            // File doesn't exist or other error, upload data
            if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                uploadUserData(storageRef)
                addUserToRealtimeDatabase(userId)

            } else {
                Log.e("Storage", "Error checking file: $e")
            }
        }
        // zwraca ref do folderu uzytkownika
        val userFolder = "users/$userId"
        return storage.reference.child(userFolder)
    }

    private fun uploadUserData(storageRef: StorageReference) {
        val data = "test data".toByteArray()
        storageRef.putBytes(data)
            .addOnSuccessListener {
                Log.d("Storage", "File uploaded successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Storage", "Error uploading data: $e")
            }
    }
    private fun addUserToRealtimeDatabase(userId: String) {
        val userRef = realtimeDatabase.reference.child("users").child(userId).child("outfits")
        userRef.setValue("test string")
            .addOnSuccessListener {
                Log.d("Database", "User added to Realtime Database successfully!")
                _userOutfitsRef = storage.reference.child("users").child(userId).child("outfits")
            }
            .addOnFailureListener { e ->
                Log.e("Database", "Error adding user to Realtime Database: $e")
            }
    }

    fun updatePageData(offset: Long, userStorageRef: StorageReference) {
        _selectedDate.value = DateUtils.changeDay(_selectedDate.value, offset)
        Log.d("DATE in fun", "updatePageData: $selectedDate")
        val chosenDateFormatted = DateUtils.formatDate(_selectedDate.value, "yyyyMMdd")
        fetchUrlsForDate( chosenDateFormatted)

    }
    fun fetchUrlsForDate(date: String) {
        val userId = userId ?: return

        val dateRef = realtimeDatabase.reference
            .child("users")
            .child(userId)
            .child("outfits")
            .child(date)

        dateRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val urls = mutableListOf<String>()

                for (outfitSnapshot in snapshot.children) {
                    val url = outfitSnapshot.getValue(String::class.java)
                    url?.let {
                        urls.add(it)
                    }
                }

                _imagesUrl.value = urls // Update MutableStateFlow with URLs
            } else {
                _imagesUrl.value = emptyList() // No data for this date
            }
        }.addOnFailureListener { e ->
            Log.e("Database", "Error fetching URLs for date $date: $e")
            _imagesUrl.value = emptyList() // Handle failure by setting empty list
        }
    }





}


