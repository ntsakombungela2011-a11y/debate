package com.jules.debate

import com.jules.debate.repository.QuotaExceededException
import org.junit.Assert.assertEquals
import org.junit.Test

class DebateRepositoryTest {
    @Test
    fun testQuotaExceededExceptionMessage() {
        val exception = QuotaExceededException()
        assertEquals("Free tier limit reached, try again in a bit", exception.message)
    }
}
