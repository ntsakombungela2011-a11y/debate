package com.jules.debate.model

data class DebateBrief(
    val topic: String,
    val neutralSummary: String,
    val forArguments: List<String>,
    val againstArguments: List<String>,
    val keyFacts: List<String>,
    val sources: List<Source>,
    val openQuestions: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class Source(
    val title: String,
    val url: String
)
