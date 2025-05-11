package com.example.textn.data.services

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CloudinaryService(private val context: Context) {

    init {
        CloudinaryConfig.init(context)
    }

    suspend fun uploadImage(imageUri: Uri): Result<String> = suspendCancellableCoroutine { continuation ->
        val requestId = MediaManager.get().upload(imageUri)
            .option("folder", "weather_app_community")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Bắt đầu tải lên
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Cập nhật tiến trình
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as? String
                    if (imageUrl != null) {
                        continuation.resume(Result.success(imageUrl))
                    } else {
                        continuation.resume(Result.failure(Exception("Không lấy được secure_url từ Cloudinary")))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resume(Result.failure(Exception(error.description)))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // Nếu cần lên lịch lại
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}