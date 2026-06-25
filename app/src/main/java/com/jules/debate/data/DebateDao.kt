package com.jules.debate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DebateDao {
    @Insert
    suspend fun insertBrief(brief: DebateBriefEntity)

    @Query("SELECT * FROM debate_briefs ORDER BY timestamp DESC")
    fun getAllBriefs(): Flow<List<DebateBriefEntity>>

    @Query("SELECT * FROM debate_briefs WHERE timestamp = :timestamp LIMIT 1")
    suspend fun getBriefByTimestamp(timestamp: Long): DebateBriefEntity?
}
