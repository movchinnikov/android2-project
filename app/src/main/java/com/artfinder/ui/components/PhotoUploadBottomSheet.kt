package com.artfinder.ui.visited

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Bottom sheet for uploading photos as per Milestone 3 requirements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploadBottomSheet(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Add Photos to Artwork",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onTakePhoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Take Photo")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onPickFromGallery,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick from Gallery")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
