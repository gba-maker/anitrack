package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ItemStatus
import com.example.data.model.ItemType
import com.example.data.model.UserItem
import com.example.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testUserProfile_createAndEnsure() = runBlocking {
        val uid = signInTestUser("tester@example.com")
        val repo = FirestoreRepository(firestore)

        val profile = withTimeout(5000L) {
            repo.ensureUserProfile("tester@example.com", "Test User")
        }

        assertNotNull(profile)
        assertEquals(uid, profile.userId)
        assertEquals("tester@example.com", profile.email)
    }

    @Test
    fun testUserItem_saveAndObserve() = runBlocking {
        val uid = signInTestUser("alice@example.com")
        val repo = FirestoreRepository(firestore)

        val item = UserItem(
            id = "anime_1",
            userId = uid,
            malId = 1L,
            title = "Cowboy Bebop",
            imageUrl = "https://example.com/poster.jpg",
            type = ItemType.ANIME,
            status = ItemStatus.WATCHING,
            currentProgress = 3L,
            totalProgress = 26L,
            userScore = 9L,
            personalNotes = "Great music",
            genres = listOf("Action", "Sci-Fi")
        )

        withTimeout(5000L) {
            repo.saveOrUpdateItem(item)
        }

        val observedItems = withTimeout(5000L) {
            repo.observeUserItems(uid).first { it.isNotEmpty() }
        }

        assertEquals(1, observedItems.size)
        assertEquals("Cowboy Bebop", observedItems[0].title)
        assertEquals(3L, observedItems[0].currentProgress)
    }

    @Test
    fun testUpdateProgress_incrementsCorrectly() = runBlocking {
        val uid = signInTestUser("bob@example.com")
        val repo = FirestoreRepository(firestore)

        val item = UserItem(
            id = "manga_11",
            userId = uid,
            malId = 11L,
            title = "Naruto",
            type = ItemType.MANGA,
            status = ItemStatus.WATCHING,
            currentProgress = 10L,
            totalProgress = 700L
        )

        withTimeout(5000L) {
            repo.saveOrUpdateItem(item)
            repo.updateProgress("manga_11", 11L)
        }

        val observedItems = withTimeout(5000L) {
            repo.observeUserItems(uid).first { it.any { it.id == "manga_11" && it.currentProgress == 11L } }
        }

        assertTrue(observedItems.any { it.id == "manga_11" && it.currentProgress == 11L })
    }
}
