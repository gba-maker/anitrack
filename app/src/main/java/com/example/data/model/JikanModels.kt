package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JikanImageFormat(
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "small_image_url") val smallImageUrl: String? = null,
    @Json(name = "large_image_url") val largeImageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class JikanImages(
    @Json(name = "jpg") val jpg: JikanImageFormat? = null
)

@JsonClass(generateAdapter = true)
data class JikanGenre(
    @Json(name = "mal_id") val malId: Long = 0L,
    @Json(name = "name") val name: String = ""
)

@JsonClass(generateAdapter = true)
data class JikanAnimeDto(
    @Json(name = "mal_id") val malId: Long = 0L,
    @Json(name = "title") val title: String = "",
    @Json(name = "title_english") val titleEnglish: String? = null,
    @Json(name = "images") val images: JikanImages? = null,
    @Json(name = "episodes") val episodes: Int? = null,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "genres") val genres: List<JikanGenre>? = null
)

@JsonClass(generateAdapter = true)
data class JikanMangaDto(
    @Json(name = "mal_id") val malId: Long = 0L,
    @Json(name = "title") val title: String = "",
    @Json(name = "title_english") val titleEnglish: String? = null,
    @Json(name = "images") val images: JikanImages? = null,
    @Json(name = "chapters") val chapters: Int? = null,
    @Json(name = "volumes") val volumes: Int? = null,
    @Json(name = "score") val score: Double? = null,
    @Json(name = "synopsis") val synopsis: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "genres") val genres: List<JikanGenre>? = null
)

@JsonClass(generateAdapter = true)
data class JikanAnimeListResponse(
    @Json(name = "data") val data: List<JikanAnimeDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class JikanMangaListResponse(
    @Json(name = "data") val data: List<JikanMangaDto> = emptyList()
)

// Unified Catalog item for UI presentation
data class CatalogItem(
    val malId: Long,
    val title: String,
    val imageUrl: String,
    val type: String, // ANIME or MANGA
    val totalCount: Int, // episodes or chapters
    val score: Double,
    val synopsis: String,
    val statusText: String,
    val genres: List<String>
)
