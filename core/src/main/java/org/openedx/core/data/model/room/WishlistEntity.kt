package org.openedx.core.data.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_table")
data class WishlistEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val image: String,
    val duration: String,
    val progress: String,
    val category: String,
    val level: String,
    val rating: String,
    val reviews: String,
    val instructor: String,
)
