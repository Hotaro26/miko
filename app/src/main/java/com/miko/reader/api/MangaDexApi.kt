package com.miko.reader.api

import com.miko.reader.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaDexApi {
    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art"),
        @Query("limit") limit: Int = 20
    ): MangaResponse

    @GET("manga/{id}/feed")
    suspend fun getMangaChapters(
        @Path("id") mangaId: String,
        @Query("translatedLanguage[]") langs: List<String> = listOf("en"),
        @Query("order[chapter]") order: String = "asc",
        @Query("limit") limit: Int = 100
    ): ChapterListResponse

    @GET("manga/{id}")
    suspend fun getManga(
        @Path("id") id: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art")
    ): MangaDataResponse

    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art"),
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive")
    ): MangaResponse

    @GET("manga/random")
    suspend fun getRandomManga(
        @Query("includes[]") includes: List<String> = listOf("cover_art")
    ): MangaDataResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getAtHomeServer(
        @Path("chapterId") chapterId: String
    ): AtHomeResponse
}

data class MangaDataResponse(val data: MangaData)
