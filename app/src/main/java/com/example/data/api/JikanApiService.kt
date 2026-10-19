package com.example.data.api

import com.example.data.model.JikanAnimeListResponse
import com.example.data.model.JikanMangaListResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface JikanApiService {

    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("filter") filter: String? = null
    ): JikanAnimeListResponse

    @GET("top/manga")
    suspend fun getTopManga(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): JikanMangaListResponse

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("sfw") sfw: Boolean = true
    ): JikanAnimeListResponse

    @GET("manga")
    suspend fun searchManga(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 25,
        @Query("sfw") sfw: Boolean = true
    ): JikanMangaListResponse

    @GET("anime")
    suspend fun getAnimeByGenres(
        @Query("genres") genres: String,
        @Query("order_by") orderBy: String = "score",
        @Query("sort") sort: String = "desc",
        @Query("limit") limit: Int = 15,
        @Query("sfw") sfw: Boolean = true
    ): JikanAnimeListResponse
}

object JikanApiClient {
    private const val BASE_URL = "https://api.jikan.moe/v4/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "AniTrackApp/1.0 (Android)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val service: JikanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JikanApiService::class.java)
    }
}
