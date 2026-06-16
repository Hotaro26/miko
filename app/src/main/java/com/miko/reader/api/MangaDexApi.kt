package com.miko.reader.api

import com.google.gson.annotations.SerializedName
import com.miko.reader.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaDexApi {
    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("includes[]") includes: List<@JvmSuppressWildcards String>,
        @Query("limit") limit: Int
    ): MangaResponse

    @GET("manga/{id}/feed")
    suspend fun getMangaChapters(
        @Path("id") mangaId: String,
        @Query("translatedLanguage[]") langs: List<@JvmSuppressWildcards String>,
        @Query("order[chapter]") order: String,
        @Query("limit") limit: Int
    ): ChapterListResponse

    @GET("manga/{id}")
    suspend fun getManga(
        @Path("id") id: String,
        @Query("includes[]") includes: List<@JvmSuppressWildcards String>
    ): MangaDataResponse

    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("includes[]") includes: List<@JvmSuppressWildcards String>,
        @Query("contentRating[]") contentRating: List<@JvmSuppressWildcards String>
    ): MangaResponse

    @GET("manga/random")
    suspend fun getRandomManga(
        @Query("includes[]") includes: List<@JvmSuppressWildcards String>
    ): MangaDataResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getAtHomeServer(
        @Path("chapterId") chapterId: String
    ): AtHomeResponse
}

data class MangaDataResponse(@SerializedName("data") val data: MangaData)
