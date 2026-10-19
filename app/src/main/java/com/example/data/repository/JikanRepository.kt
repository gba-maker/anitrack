package com.example.data.repository

import com.example.data.api.JikanApiClient
import com.example.data.api.JikanApiService
import com.example.data.model.CatalogItem
import com.example.data.model.ItemType
import com.example.data.model.JikanAnimeDto
import com.example.data.model.JikanMangaDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JikanRepository(
    private val api: JikanApiService = JikanApiClient.service
) {

    suspend fun getTop(type: String): Result<List<CatalogItem>> = withContext(Dispatchers.IO) {
        try {
            if (type == ItemType.MANGA) {
                val response = api.getTopManga(limit = 20)
                val items = response.data.map { it.toCatalogItem() }
                Result.success(items)
            } else {
                val response = api.getTopAnime(limit = 20)
                val items = response.data.map { it.toCatalogItem() }
                Result.success(items)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun search(query: String, type: String): Result<List<CatalogItem>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext getTop(type)
        }
        try {
            if (type == ItemType.MANGA) {
                val response = api.searchManga(query = query, limit = 25)
                val items = response.data.map { it.toCatalogItem() }
                Result.success(items)
            } else {
                val response = api.searchAnime(query = query, limit = 25)
                val items = response.data.map { it.toCatalogItem() }
                Result.success(items)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Personalized recommendations based on the user's top genres
     * and excluding works already in the user's library.
     */
    suspend fun getPersonalizedRecommendations(
        preferredGenres: List<String>,
        existingMalIds: Set<Long>
    ): Result<List<CatalogItem>> = withContext(Dispatchers.IO) {
        try {
            // Fetch top anime to sample candidates
            val topResponse = api.getTopAnime(limit = 25)
            val candidates = topResponse.data.map { it.toCatalogItem() }

            // Filter out items already tracked
            val unvisited = candidates.filter { it.malId !in existingMalIds }

            // Rank by matching genre overlap
            val sorted = if (preferredGenres.isNotEmpty()) {
                unvisited.sortedByDescending { item ->
                    item.genres.count { it in preferredGenres }
                }
            } else {
                unvisited
            }

            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun JikanAnimeDto.toCatalogItem(): CatalogItem {
        val img = images?.jpg?.largeImageUrl
            ?: images?.jpg?.imageUrl
            ?: images?.jpg?.smallImageUrl
            ?: ""
        val genreNames = genres?.map { it.name } ?: emptyList()
        val titleText = if (!titleEnglish.isNullOrBlank()) titleEnglish else title
        return CatalogItem(
            malId = malId,
            title = titleText.ifBlank { "Titre inconnu" },
            imageUrl = img,
            type = ItemType.ANIME,
            totalCount = episodes ?: 0,
            score = score ?: 0.0,
            synopsis = synopsis ?: "Aucun synopsis disponible.",
            statusText = status ?: "Inconnu",
            genres = genreNames
        )
    }

    private fun JikanMangaDto.toCatalogItem(): CatalogItem {
        val img = images?.jpg?.largeImageUrl
            ?: images?.jpg?.imageUrl
            ?: images?.jpg?.smallImageUrl
            ?: ""
        val genreNames = genres?.map { it.name } ?: emptyList()
        val titleText = if (!titleEnglish.isNullOrBlank()) titleEnglish else title
        return CatalogItem(
            malId = malId,
            title = titleText.ifBlank { "Titre inconnu" },
            imageUrl = img,
            type = ItemType.MANGA,
            totalCount = chapters ?: (volumes ?: 0),
            score = score ?: 0.0,
            synopsis = synopsis ?: "Aucun synopsis disponible.",
            statusText = status ?: "Inconnu",
            genres = genreNames
        )
    }
}
