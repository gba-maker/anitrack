package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val isPremium: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object ItemStatus {
    const val WATCHING = "WATCHING" // Or READING for manga
    const val COMPLETED = "COMPLETED"
    const val ON_HOLD = "ON_HOLD"
    const val PLAN_TO_WATCH = "PLAN_TO_WATCH"

    fun getDisplayName(status: String, isManga: Boolean): String {
        return when (status) {
            WATCHING -> if (isManga) "En lecture" else "En cours"
            COMPLETED -> "Terminé"
            ON_HOLD -> "En pause"
            PLAN_TO_WATCH -> if (isManga) "À lire" else "À voir"
            else -> status
        }
    }
}

object ItemType {
    const val ANIME = "ANIME"
    const val MANGA = "MANGA"
}

@IgnoreExtraProperties
data class UserItem(
    val id: String = "",
    val userId: String = "",
    val malId: Long = 0L,
    val title: String = "",
    val imageUrl: String = "",
    val type: String = ItemType.ANIME,
    val status: String = ItemStatus.WATCHING,
    val currentProgress: Long = 0L,
    val totalProgress: Long = 0L,
    val userScore: Long = 0L,
    val personalNotes: String = "",
    val genres: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
