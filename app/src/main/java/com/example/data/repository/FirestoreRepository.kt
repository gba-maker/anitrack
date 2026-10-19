package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.UserItem
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserItems(uid: String): Flow<List<UserItem>> = flow {
        val path = "users/$uid/items"
        db.collection("users").document(uid).collection("items")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(UserItem::class.java)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
            .collect { emit(it) }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
        val path = "users/$uid"
        db.collection("users").document(uid)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java)
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, path)
                }
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun ensureUserProfile(email: String, displayName: String): UserProfile {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        if (snapshot.exists()) {
            return snapshot.toObject(UserProfile::class.java) ?: UserProfile(userId = uid, email = email, displayName = displayName)
        } else {
            val initialProfile = UserProfile(
                userId = uid,
                email = email,
                displayName = displayName,
                isPremium = false
            )
            val payload = mapOf(
                "userId" to uid,
                "email" to email,
                "displayName" to displayName,
                "isPremium" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
            return initialProfile
        }
    }

    suspend fun setPremiumStatus(isPremium: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "isPremium" to isPremium,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun saveOrUpdateItem(item: UserItem) {
        val uid = requireUserId()
        val itemId = if (item.id.isNotBlank()) item.id else "${item.type.lowercase()}_${item.malId}"
        val docRef = db.collection("users").document(uid).collection("items").document(itemId)

        val existing = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }

        val payload = mutableMapOf<String, Any>(
            "id" to itemId,
            "userId" to uid,
            "malId" to item.malId,
            "title" to item.title,
            "imageUrl" to item.imageUrl,
            "type" to item.type,
            "status" to item.status,
            "currentProgress" to item.currentProgress,
            "totalProgress" to item.totalProgress,
            "userScore" to item.userScore,
            "personalNotes" to item.personalNotes,
            "genres" to item.genres,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (existing == null || !existing.exists()) {
            payload["createdAt"] = FieldValue.serverTimestamp()
            try {
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        } else {
            val existingCreatedAt = existing.getTimestamp("createdAt")
            if (existingCreatedAt != null) {
                payload["createdAt"] = existingCreatedAt
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }
            try {
                docRef.set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        }
    }

    suspend fun updateProgress(itemId: String, newProgress: Long) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("items").document(itemId)
        val payload = mapOf(
            "currentProgress" to newProgress,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateStatus(itemId: String, newStatus: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("items").document(itemId)
        val payload = mapOf(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateRatingAndNotes(itemId: String, score: Long, notes: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("items").document(itemId)
        val payload = mapOf(
            "userScore" to score,
            "personalNotes" to notes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteItem(itemId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
