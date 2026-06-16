package com.miko.reader.model

import com.google.gson.annotations.SerializedName

data class MangaResponse(
    @SerializedName("data") val data: List<MangaData>
)

data class MangaData(
    @SerializedName("id") val id: String,
    @SerializedName("attributes") val attributes: MangaAttributes,
    @SerializedName("relationships") val relationships: List<Relationship>
) {
    fun getCoverUrl(size: String = "256"): String? {
        val fileName = relationships.find { it.type == "cover_art" }?.attributes?.fileName
        return if (!fileName.isNullOrEmpty()) {
            "https://uploads.mangadex.org/covers/$id/$fileName.$size.jpg"
        } else null
    }

    fun getDescription(): String {
        return when (val desc = attributes.description) {
            is Map<*, *> -> desc["en"]?.toString() ?: desc.values.firstOrNull()?.toString() ?: ""
            else -> ""
        }
    }

    fun getTitle(): String {
        return attributes.title["en"] ?: attributes.title.values.firstOrNull() ?: "Unknown"
    }
}

data class MangaAttributes(
    @SerializedName("title") val title: Map<String, String>,
    @SerializedName("description") val description: Any?, // Can be Map or empty Array []
    @SerializedName("lastChapter") val lastChapter: String?
)

data class Relationship(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: RelationshipAttributes? = null
)

data class RelationshipAttributes(
    @SerializedName("fileName") val fileName: String? = null
)

data class ChapterListResponse(
    @SerializedName("data") val data: List<ChapterData>
)

data class ChapterData(
    @SerializedName("id") val id: String,
    @SerializedName("attributes") val attributes: ChapterAttributes
)

data class ChapterAttributes(
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("externalUrl") val externalUrl: String?
)

data class AtHomeResponse(
    @SerializedName("baseUrl") val baseUrl: String,
    @SerializedName("chapter") val chapter: AtHomeChapter
)

data class AtHomeChapter(
    @SerializedName("hash") val hash: String,
    @SerializedName("data") val data: List<String>
)
