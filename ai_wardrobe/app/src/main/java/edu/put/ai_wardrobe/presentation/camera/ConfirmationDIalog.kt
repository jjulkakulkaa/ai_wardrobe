package edu.put.ai_wardrobe.presentation.camera

import edu.put.ai_wardrobe.ui.theme.CutePink
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmationDialog(
    bitmap: Bitmap,
    clothType: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    var selectedClothType by remember { mutableStateOf(clothType) }

    val clothTypes = listOf(
        "T-shirt", "Trouser", "Pullover", "Dress", "Coat",
        "Sandal", "Shirt", "Sneaker", "Bag", "Ankle Boot"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirm Clothing Type") },
        text = {
            Column {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Text(text = "Is this a $clothType?", modifier = Modifier.padding(top = 16.dp), color = CutePink)

                Spacer(modifier = Modifier.height(16.dp))

                if (showDropdown) {
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        clothTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type) },
                                onClick = {
                                    selectedClothType = type
                                    onConfirm(selectedClothType)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedClothType) }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDropdown = true }) {
                Text("No")
            }
        }
    )
}
