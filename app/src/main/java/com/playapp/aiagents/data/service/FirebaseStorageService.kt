package com.playapp.aiagents.data.service

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.InputStream

class FirebaseStorageService {
    private val TAG = "FirebaseStorageService"
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // Upload file from local file path
    suspend fun uploadFile(localFilePath: String, remotePath: String): Result<String> {
        return try {
            val file = Uri.fromFile(File(localFilePath))
            val ref = storageRef.child(remotePath)
            val uploadTask = ref.putFile(file).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file", e)
            Result.failure(e)
        }
    }

    // Upload file from InputStream
    suspend fun uploadFile(inputStream: InputStream, remotePath: String, contentType: String? = null): Result<String> {
        return try {
            val ref = storageRef.child(remotePath)
            val uploadTask = if (contentType != null) {
                ref.putStream(inputStream, com.google.firebase.storage.StorageMetadata.Builder()
                    .setContentType(contentType)
                    .build())
            } else {
                ref.putStream(inputStream)
            }.await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file from stream", e)
            Result.failure(e)
        }
    }

    // Download file to local path
    suspend fun downloadFile(remotePath: String, localFilePath: String): Result<Unit> {
        return try {
            val ref = storageRef.child(remotePath)
            val localFile = File(localFilePath)
            ref.getFile(localFile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading file", e)
            Result.failure(e)
        }
    }

    // Get download URL for a file
    suspend fun getDownloadUrl(remotePath: String): Result<String> {
        return try {
            val ref = storageRef.child(remotePath)
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting download URL", e)
            Result.failure(e)
        }
    }

    // Delete file
    suspend fun deleteFile(remotePath: String): Result<Unit> {
        return try {
            val ref = storageRef.child(remotePath)
            ref.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            Result.failure(e)
        }
    }

    // List files in a directory
    suspend fun listFiles(remotePath: String): Result<List<StorageReference>> {
        return try {
            val ref = storageRef.child(remotePath)
            val result = ref.listAll().await()
            Result.success(result.items)
        } catch (e: Exception) {
            Log.e(TAG, "Error listing files", e)
            Result.failure(e)
        }
    }

    // Get file metadata
    suspend fun getFileMetadata(remotePath: String): Result<com.google.firebase.storage.StorageMetadata> {
        return try {
            val ref = storageRef.child(remotePath)
            val metadata = ref.metadata.await()
            Result.success(metadata)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file metadata", e)
            Result.failure(e)
        }
    }

    // Upload code example files
    suspend fun uploadCodeExample(agentId: Int, fileName: String, content: String): Result<String> {
        return try {
            val remotePath = "code_examples/$agentId/$fileName"
            val inputStream = content.byteInputStream()
            uploadFile(inputStream, remotePath, "text/plain")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading code example", e)
            Result.failure(e)
        }
    }

    // Upload video tutorial
    suspend fun uploadVideoTutorial(agentId: Int, videoFilePath: String): Result<String> {
        return try {
            val fileName = File(videoFilePath).name
            val remotePath = "video_tutorials/$agentId/$fileName"
            uploadFile(videoFilePath, remotePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading video tutorial", e)
            Result.failure(e)
        }
    }
}