package com.example.corescanner.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.0-flash-lite:generateContent")
    suspend fun askToGemini(
        @Query("key") apiKey: String,
        @Body requestBody: RequestBody
    ): Response<ResponseData>
}

data class RequestBody(val contents: List<ContentBody>)

data class ContentBody(val parts: List<PartBody>)

data class PartBody(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String
)

data class ResponseData(
    val candidates: List<ResponseCandidate>?
)

data class ResponseCandidate(
    val content: ResponseContent?,
    val finishReason: String? = null,
    val safetyRatings: List<ResponseSafetyRating>? = null
)

data class ResponseContent(val parts: List<ResponsePart>?)

data class ResponsePart(val text: String?)

data class ResponseSafetyRating(val category: String?, val probability: String?)
