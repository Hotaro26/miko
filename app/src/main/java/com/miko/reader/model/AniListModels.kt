package com.miko.reader.model

import com.google.gson.annotations.SerializedName

data class AniListRequest(
    @SerializedName("query") val query: String,
    @SerializedName("variables") val variables: Map<String, Any> = emptyMap()
)

data class AniListResponse<T>(
    @SerializedName("data") val data: T
)

data class AniListSearchData(
    @SerializedName("Page") val page: AniListPage
)

data class AniListMediaData(
    @SerializedName("Media") val media: AniListMedia
)

data class ViewerResponse(
    @SerializedName("Viewer") val viewer: AniListUser?
)

data class AniListUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: AniListUserAvatar?
)

data class AniListUserAvatar(
    @SerializedName("large") val large: String?
)

data class AniListPage(
    @SerializedName("media") val media: List<AniListMedia>
)

data class AniListMedia(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: AniListTitle,
    @SerializedName("status") val status: String?,
    @SerializedName("startDate") val startDate: AniListDate?,
    @SerializedName("averageScore") val averageScore: Int?,
    @SerializedName("description") val description: String?,
    @SerializedName("coverImage") val coverImage: AniListCoverImage?
)

data class AniListTitle(
    @SerializedName("english") val english: String?,
    @SerializedName("romaji") val romaji: String?,
    @SerializedName("native") val native: String?
) {
    fun userPreferred(): String = english ?: romaji ?: native ?: "Unknown"
}

data class AniListDate(
    @SerializedName("year") val year: Int?,
    @SerializedName("month") val month: Int?,
    @SerializedName("day") val day: Int?
) {
    override fun toString(): String {
        return if (year != null) {
            listOfNotNull(year, month, day).joinToString("-")
        } else "Unknown"
    }
}

data class AniListCoverImage(
    @SerializedName("large") val large: String?,
    @SerializedName("medium") val medium: String?
)
