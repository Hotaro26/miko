package com.miko.reader.api

import com.miko.reader.model.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AniListApi {
    @POST("/")
    suspend fun post(@Body request: AniListRequest): AniListResponse<AniListSearchData>

    @POST("/")
    suspend fun getMedia(@Body request: AniListRequest): AniListResponse<AniListMediaData>

    @POST("/")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String,
        @Body request: AniListRequest
    ): AniListResponse<ViewerResponse>

    @POST("/")
    suspend fun getReleasing(@Body request: AniListRequest): AniListResponse<AniListSearchData>

    companion object {
        const val VIEWER_QUERY = """
            query {
                Viewer {
                    id
                    name
                    avatar {
                        large
                    }
                }
            }
        """

        const val SEARCH_QUERY = """
            query (${"$"}search: String, ${"$"}page: Int, ${"$"}perPage: Int, ${"$"}sort: [MediaSort], ${"$"}status: MediaStatus) {
                Page(page: ${"$"}page, perPage: ${"$"}perPage) {
                    media(search: ${"$"}search, type: MANGA, sort: ${"$"}sort, status: ${"$"}status) {
                        id
                        title {
                            romaji
                            english
                            native
                        }
                        status
                        startDate {
                            year
                            month
                            day
                        }
                        averageScore
                        description
                        coverImage {
                            large
                        }
                    }
                }
            }
        """

        const val MEDIA_QUERY = """
            query (${"$"}search: String) {
                Media(search: ${"$"}search, type: MANGA) {
                    id
                    title {
                        romaji
                        english
                        native
                    }
                    status
                    startDate {
                        year
                        month
                        day
                    }
                    averageScore
                    description
                    coverImage {
                        large
                    }
                }
            }
        """
    }
}
