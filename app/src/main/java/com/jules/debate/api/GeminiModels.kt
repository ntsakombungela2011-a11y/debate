package com.jules.debate.api

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val role: String? = null,
    val parts: List<Part>
)

data class Part(
    val text: String? = null
)

data class Tool(
    @SerializedName("google_search_retrieval")
    val googleSearchRetrieval: GoogleSearchRetrieval? = null
)

data class GoogleSearchRetrieval(
    val dynamicRetrievalConfig: DynamicRetrievalConfig? = null
)

data class DynamicRetrievalConfig(
    val mode: String = "MODE_DYNAMIC",
    val dynamicThreshold: Float = 0.3f
)

data class GenerationConfig(
    val responseMimeType: String? = "application/json",
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null
)

data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata?
)

data class Candidate(
    val content: Content,
    val finishReason: String?,
    val groundingMetadata: GroundingMetadata?
)

data class GroundingMetadata(
    val searchEntryPoint: SearchEntryPoint?,
    val groundingChunks: List<GroundingChunk>?,
    val groundingSupports: List<GroundingSupport>?
)

data class SearchEntryPoint(
    val renderedContent: String?
)

data class GroundingChunk(
    val web: Web?
)

data class Web(
    val uri: String?,
    val title: String?
)

data class GroundingSupport(
    val segment: Segment?,
    val groundingChunkIndices: List<Int>?,
    val confidenceScores: List<Float>?
)

data class Segment(
    val startIndex: Int?,
    val endIndex: Int?,
    val text: String?
)

data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)
