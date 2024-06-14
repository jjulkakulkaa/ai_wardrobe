package edu.put.ai_wardrobe.presentation.planner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import edu.put.ai_wardrobe.presentation.sign_in.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.math.log

class PlannerViewModel(
    private val storage: FirebaseStorage,
    userData: UserData?
) : ViewModel() {

    val userId: String? = userData?.userId

    val realtimeDatabase = Firebase.database("https://aiwardrobe-a3502-default-rtdb.firebaseio.com/")
    val userOutfitsRef = realtimeDatabase.reference.child("users").child(userId!!).child("outfits")


    private val _clothesList = MutableStateFlow<List<String>>(emptyList())
    val clothesList = _clothesList.asStateFlow()


    init {
        userId?.let { fetchClothes(it, null) }
    }

    fun fetchClothes(userId: String, clothType: String?) {
        val path = if (clothType != null) {
            "users/$userId/clothes/$clothType"
        } else {
            "users/$userId/clothes"
        }
        val userClothesRef = storage.reference.child(path)
        Log.d("UserClothesRef", "fetchClothes: $userClothesRef")
        listAllFiles(userClothesRef) { allRefs ->
            fetchDownloadUrls(allRefs)
        }
    }

    private fun listAllFiles(directoryRef: StorageReference, onComplete: (List<StorageReference>) -> Unit) {
        directoryRef.listAll()
            .addOnSuccessListener { result ->
                val items = result.items
                Log.d("items in folder", "listAllFiles: $items")

                val prefixes = result.prefixes
                Log.d("prefixes in folder", "listAllFiles: $prefixes")

                val allRefs = mutableListOf<StorageReference>()

                allRefs.addAll(items)

                if (prefixes.isEmpty()) {
                    onComplete(allRefs)
                } else {
                    var remainingPrefixes = prefixes.size
                    prefixes.forEach { prefix ->
                        listAllFiles(prefix) { nestedItems ->
                            allRefs.addAll(nestedItems)
                            remainingPrefixes -= 1
                            if (remainingPrefixes == 0) {
                                onComplete(allRefs)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PlannerViewModel", "Error fetching nested clothes: $exception")
                onComplete(emptyList())
            }
    }

    private fun fetchDownloadUrls(references: List<StorageReference>) {
        viewModelScope.launch {
            val urls = references.mapNotNull { ref ->
                try {
                    ref.downloadUrl.await().toString()
                } catch (e: Exception) {
                    Log.e("PlannerViewModel", "Error fetching download URL: $e")
                    null
                }
            }
            _clothesList.value = urls
        }
    }


    fun uploadUserOutfit(firebaseRef: DatabaseReference, clothesUrls: List<String>, timestamp: String) {
        val outfitDirRef = firebaseRef.child(timestamp)
        viewModelScope.launch {
            clothesUrls.forEachIndexed {id, url ->
                try {
                    val ref = outfitDirRef.child(id.toString())
                    ref.setValue(url).await()
                    Log.d("Firebase realtime", "File uploaded successfully: $url")
                } catch (e: Exception) {
                    Log.e("Firebase realtime", "Error uploading data: $e")
                }
            }
        }
    }



}
