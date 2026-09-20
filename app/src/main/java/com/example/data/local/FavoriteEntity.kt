package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "POST" or "IMAGE"
    val title: String,
    val description: String = "",
    val thumbnail: String = "",
    val category: String = "",
    val stepsNumbers: String = "1 Step",
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
    val savedAt: Long = System.currentTimeMillis()
)
