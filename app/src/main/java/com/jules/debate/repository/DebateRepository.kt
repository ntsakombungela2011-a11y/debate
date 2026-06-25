package com.jules.debate.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.jules.debate.api.*
import com.jules.debate.data.DebateDao
import com.jules.debate.data.toDomain
import com.jules.debate.data.toEntity
import com.jules.debate.model.DebateBrief
import com.jules.debate.model.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class DebateRepository(
    private val apiService: GeminiApiService,
    private val debateDao: DebateDao,
    private val apiKey: String
) {
    private val gson = Gson()

    val history: Flow<List<DebateBrief>> = debateDao.getAllBriefs().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun fetchDebateBrief(topic: String): Result<DebateBrief> {
        val prompt = """
            You are a research assistant producing a structured debate brief. Research the user's topic using Google Search grounding. Return ONLY a single JSON object matching this exact schema, with no preamble, no markdown code fences, and no text outside the JSON:

            {
              "topic": "string",
              "neutralSummary": "string",
              "forArguments": ["string"],
              "againstArguments": ["string"],
              "keyFacts": ["string"],
              "sources": [{"title": "string", "url": "string"}],
              "openQuestions": ["string"]
            }

            Present the strongest good-faith version of each side's case, not strawmen. Use only real sources returned by search grounding — never invent a URL. If the topic isn't a "for/against" debate (e.g. a factual question), leave forArguments/againstArguments empty and put the answer in neutralSummary.

            USER TOPIC:
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            tools = listOf(Tool(googleSearchRetrieval = GoogleSearchRetrieval(DynamicRetrievalConfig()))),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            if (response.isSuccessful) {
                val body = response.body()
                val candidate = body?.candidates?.firstOrNull()
                val text = candidate?.content?.parts?.firstOrNull()?.text

                if (text != null) {
                    val parsedBrief = parseAndMapBrief(text, candidate)
                    if (parsedBrief != null) {
                        debateDao.insertBrief(parsedBrief.toEntity())
                        Result.success(parsedBrief)
                    } else {
                        Result.failure(Exception("Failed to parse debate brief"))
                    }
                } else {
                    Result.failure(Exception("Empty response from Gemini"))
                }
            } else if (response.code() == 429) {
                Result.failure(QuotaExceededException())
            } else {
                Result.failure(Exception("API call failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAndMapBrief(jsonText: String, candidate: Candidate): DebateBrief? {
        return try {
            val cleanJson = sanitizeJson(jsonText)
            val brief = gson.fromJson(cleanJson, DebateBrief::class.java)

            // Map sources from groundingMetadata if they are better/more reliable
            val groundingSources = candidate.groundingMetadata?.groundingChunks?.mapNotNull { chunk ->
                val web = chunk.web
                if (web?.uri != null && web.title != null) {
                    Source(web.title, web.uri)
                } else null
            } ?: emptyList()

            // Merge or replace sources with grounded ones
            val finalSources = if (groundingSources.isNotEmpty()) groundingSources else brief.sources

            brief.copy(sources = finalSources, timestamp = System.currentTimeMillis())
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun sanitizeJson(text: String): String {
        var sanitized = text.trim()
        if (sanitized.startsWith("```json")) {
            sanitized = sanitized.removePrefix("```json")
        }
        if (sanitized.startsWith("```")) {
            sanitized = sanitized.removePrefix("```")
        }
        if (sanitized.endsWith("```")) {
            sanitized = sanitized.removeSuffix("```")
        }
        return sanitized.trim()
    }

    suspend fun getBrief(timestamp: Long): DebateBrief? {
        return debateDao.getBriefByTimestamp(timestamp)?.toDomain()
    }
}

class QuotaExceededException : Exception("Free tier limit reached, try again in a bit")
