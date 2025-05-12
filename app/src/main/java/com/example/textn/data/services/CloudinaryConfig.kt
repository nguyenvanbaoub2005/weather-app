package com.example.textn.data.services

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.policy.GlobalUploadPolicy

object CloudinaryConfig {

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to "dtayjf0fi",
                "api_key" to "871873839857943",
                "api_secret" to "0KJ2kjIRDHPBN7nxWplbJkCVpVo",
                "secure" to true
            )

            MediaManager.init(context.applicationContext, config)

            val globalUploadPolicy = GlobalUploadPolicy.Builder()
                .maxRetries(3)
                .build()

            MediaManager.get().setGlobalUploadPolicy(globalUploadPolicy)

            isInitialized = true
        }
    }
}
