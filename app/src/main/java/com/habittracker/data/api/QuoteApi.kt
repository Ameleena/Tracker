package com.habittracker.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteApi {
    @GET("quotes/random")
    suspend fun getRandomQuote(
        @Query("maxLength") maxLength: Int = 150,
        @Query("tags") tags: String = "motivation|success|inspiration",
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): QuoteResponse
    
    @GET("quotes")
    suspend fun getQuotesByCategory(
        @Query("category") category: String,
        @Query("maxLength") maxLength: Int = 150,
        @Query("_") timestamp: Long = System.currentTimeMillis()
    ): QuoteListResponse
} 