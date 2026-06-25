package com.jules.debate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jules.debate.model.DebateBrief
import com.jules.debate.model.Source

@Entity(tableName = "debate_briefs")
data class DebateBriefEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val neutralSummary: String,
    val forArgumentsJson: String,
    val againstArgumentsJson: String,
    val keyFactsJson: String,
    val sourcesJson: String,
    val openQuestionsJson: String,
    val timestamp: Long
)

fun DebateBrief.toEntity(): DebateBriefEntity {
    val gson = Gson()
    return DebateBriefEntity(
        topic = topic,
        neutralSummary = neutralSummary,
        forArgumentsJson = gson.toJson(forArguments),
        againstArgumentsJson = gson.toJson(againstArguments),
        keyFactsJson = gson.toJson(keyFacts),
        sourcesJson = gson.toJson(sources),
        openQuestionsJson = gson.toJson(openQuestions),
        timestamp = timestamp
    )
}

fun DebateBriefEntity.toDomain(): DebateBrief {
    val gson = Gson()
    val stringListType = object : TypeToken<List<String>>() {}.type
    val sourceListType = object : TypeToken<List<Source>>() {}.type

    return DebateBrief(
        topic = topic,
        neutralSummary = neutralSummary,
        forArguments = gson.fromJson(forArgumentsJson, stringListType),
        againstArguments = gson.fromJson(againstArgumentsJson, stringListType),
        keyFacts = gson.fromJson(keyFactsJson, stringListType),
        sources = gson.fromJson(sourcesJson, sourceListType),
        openQuestions = gson.fromJson(openQuestionsJson, stringListType),
        timestamp = timestamp
    )
}
