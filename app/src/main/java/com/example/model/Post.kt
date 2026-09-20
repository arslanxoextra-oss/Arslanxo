package com.example.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class Post(
    @DocumentId
    val id: String = "",
    val thumbnail: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val description: String = "",
    val text: String = "",
    val category: String = "",
    val StepsNumbers: String = "1 Step",
    val step1title: String = "",
    val step1description: String = "",
    val step1prompt: String = "",
    val step1toollink: String = "",
    val step1toolname: String = "",
    val step2title: String = "",
    val step2description: String = "",
    val step2prompt: String = "",
    val step2toollink: String = "",
    val step2toolname: String = "",
    val has2bean: Boolean = false,
    val homeCategory: String = "All",
    val createdAt: Any? = null,
    val createdTime: Any? = null,
    val createTime: Any? = null,
    val created_at: Any? = null,
    val timestamp: Any? = null
) {
    val displayThumbnail: String
        get() = thumbnail.ifBlank { imageUrl }

    val displayDescription: String
        get() = description.ifBlank { text }

    val creationTimestamp: Long
        get() = com.example.util.DateUtils.parseTimestampMillis(
            createdAt ?: createdTime ?: createTime ?: created_at ?: timestamp
        )
}
