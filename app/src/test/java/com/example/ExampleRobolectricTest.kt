package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TriviaQuestionBank
import com.example.model.PointTier
import com.example.model.TriviaCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ClueGrid", appName)
    }

    @Test
    fun `question bank covers all categories and tiers`() {
        TriviaCategory.entries.forEach { category ->
            PointTier.entries.forEach { tier ->
                val count = TriviaQuestionBank.questions.count {
                    it.category == category && it.tier == tier
                }
                assertTrue("Expected questions for $category $tier", count >= 3)
            }
        }
    }

    @Test
    fun `tier progression and penalty scaling are balanced`() {
        assertEquals(PointTier.TIER_30, PointTier.TIER_10.nextTier())
        assertEquals(PointTier.TIER_50, PointTier.TIER_30.nextTier())
        assertEquals(PointTier.TIER_100, PointTier.TIER_50.nextTier())
        assertEquals(null, PointTier.TIER_100.nextTier())

        assertEquals(5, PointTier.TIER_10.penalty)
        assertEquals(15, PointTier.TIER_30.penalty)
        assertEquals(25, PointTier.TIER_50.penalty)
        assertEquals(50, PointTier.TIER_100.penalty)
    }
}
