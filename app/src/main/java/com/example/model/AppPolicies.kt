package com.example.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class AppPolicies(
    @DocumentId
    val id: String = "",
    val whatsappchannel: String = "",
    val rateus: String = "",
    val privatepolicies: String = ""
)
