package com.example

import com.example.data.GameRepository
import com.example.data.TriviaQuestionBank
import com.example.model.PointTier
import com.example.model.TriviaCategory
import com.example.ui.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun questionBank_coversAllCategoriesAndTiers() {
        TriviaCategory.entries.forEach { category ->
            PointTier.entries.forEach { tier ->
                val pool = TriviaQuestionBank.questions.filter {
                    it.category == category && it.tier == tier
                }
                assertTrue("Expected at least 3 clues for $category $tier", pool.size >= 3)
                pool.forEach { clue ->
                    assertEquals(4, clue.options.size)
                    assertTrue(clue.correctIndex in 0..3)
                }
            }
        }
    }

    @Test
    fun tierProgression_andScaledPenalties_areConsistent() {
        assertEquals(PointTier.TIER_30, PointTier.TIER_10.nextTier())
        assertEquals(PointTier.TIER_50, PointTier.TIER_30.nextTier())
        assertEquals(PointTier.TIER_100, PointTier.TIER_50.nextTier())
        assertNull(PointTier.TIER_100.nextTier())

        assertEquals(5, PointTier.TIER_10.penalty)
        assertEquals(15, PointTier.TIER_30.penalty)
        assertEquals(25, PointTier.TIER_50.penalty)
        assertEquals(50, PointTier.TIER_100.penalty)
    }

    @Test
    fun validateAnswer_acceptsOnlyExactCorrectOption() {
        val question = TriviaQuestionBank.questions.first()
        assertTrue(GameViewModel.validateAnswer(question, question.correctIndex))
        val wrongIndex = (question.correctIndex + 1) % question.options.size
        assertFalse(GameViewModel.validateAnswer(question, wrongIndex))
        assertFalse(GameViewModel.validateAnswer(question, -1))
    }

    @Test
    fun usernameSanitization_trimsAndCapsLength() {
        assertEquals("Alex Quiz", GameRepository.sanitizeUsername("  Alex   Quiz  "))
        assertEquals(24, GameRepository.sanitizeUsername("A".repeat(40)).length)
    }
}
