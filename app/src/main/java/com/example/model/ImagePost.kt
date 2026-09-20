package com.example.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class ImagePost(
    @DocumentId
    val id: String = "",
    val imagetitle: String = "",
    val title: String = "",
    val imageprompt: String = "",
    val prompt: String = "",
    val imagetoollink: String = "",
    val imagetoolname: String = "ChatGPT",
    val thumbnail: String = "",
    val imageUrl: String = "",
    val categoryforall: String = "All",
    val category: String = "",
    val categoryfornew: String = "",
    val categoryfortrending: String = "",
    val createdAt: Any? = null,
    val createdTime: Any? = null,
    val createTime: Any? = null,
    val created_at: Any? = null,
    val timestamp: Any? = null
) {
    val displayTitle: String
        get() = imagetitle.ifBlank { title.ifBlank { "AI Prompt Art" } }

    val displayPrompt: String
        get() = imageprompt.ifBlank { prompt }

    val displayThumbnail: String
        get() = thumbnail.ifBlank { imageUrl }

    val displayCategory: String
        get() = categoryforall.ifBlank { category.ifBlank { "All" } }

    val creationTimestamp: Long
        get() = com.example.util.DateUtils.parseTimestampMillis(
            createdAt ?: createdTime ?: createTime ?: created_at ?: timestamp
        )
}
