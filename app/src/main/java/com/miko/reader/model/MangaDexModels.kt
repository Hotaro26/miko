package com.miko.reader.model

data class MangaResponse(
    val data: List<MangaData>
)

data class MangaData(
    val id: String,
    val attributes: MangaAttributes,
    val relationships: List<Relationship>
)

data class MangaAttributes(
    val title: Map<String, String>,
    val description: Map<String, String>,
    val lastChapter: String?
)

data class Relationship(
    val id: String,
    val type: String,
    val attributes: CoverAttributes? = null
)

data class CoverAttributes(
    val fileName: String
)

data class ChapterListResponse(
    val data: List<ChapterData>
)

data class ChapterData(
    val id: String,
    val attributes: ChapterAttributes
)

data class ChapterAttributes(
    val chapter: String?,
    val title: String?,
    val externalUrl: String?
)

data class AtHomeResponse(
    val baseUrl: String,
    val chapter: AtHomeChapter
)

data class AtHomeChapter(
    val hash: String,
    val data: List<String>
)
