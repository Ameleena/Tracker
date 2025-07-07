package com.habittracker.data.api

data class QuoteResponse(
    val _id: String,
    val content: String,
    val author: String,
    val tags: List<String> = emptyList()
)

data class QuoteListResponse(
    val results: List<QuoteResponse>
) 