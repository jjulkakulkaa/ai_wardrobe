package edu.put.ai_wardrobe.presentation.wardrobe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.put.ai_wardrobe.presentation.sign_in.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WardrobeViewModel(
    private val storage: FirebaseStorage,
    userData: UserData?
) : ViewModel() {

    val userId: String? = userData?.userId

    private val _clothesList = MutableStateFlow<List<String>>(emptyList())
    val clothesList = _clothesList.asStateFlow()

    init {
        userId?.let { fetchClothes(it) }
    }

    private fun fetchClothes(userId: String) {
        val userClothesRef = storage.reference.child("users/$userId/clothes")
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
                Log.e("WardrobeViewModel", "Error fetching nested clothes: $exception")
                onComplete(emptyList())
            }
    }

    fun filterClothes(type: String) {
        val userClothesRef = storage.reference.child("users/$userId/clothes/$type")
        listAllFiles(userClothesRef) { allRefs ->
            fetchDownloadUrls(allRefs)
        }
    }

    private fun fetchDownloadUrls(references: List<StorageReference>) {
        viewModelScope.launch {
            val urls = references.mapNotNull { ref ->
                try {
                    ref.downloadUrl.await().toString()
                } catch (e: Exception) {
                    Log.e("WardrobeViewModel", "Error fetching download URL: $e")
                    null
                }
            }
            _clothesList.value = urls
        }
    }

    fun deleteItemFromFirebase(url: String) {
        val storageRef = storage.getReferenceFromUrl(url)
        storageRef.delete()
            .addOnSuccessListener {
                Log.d("WardrobeViewModel", "Item deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("WardrobeViewModel", "Error deleting item: $e")
            }
    }
}
