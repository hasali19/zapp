package dev.hasali.zapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PackageSource(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val url: String,
)