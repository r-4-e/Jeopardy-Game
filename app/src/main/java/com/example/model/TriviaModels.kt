package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TriviaCategory(
    val key: String,
    val label: String,
    val shortLabel: String
) {
    GEOGRAPHY("GEOGRAPHY", "Geography", "GEO"),
    MATHS("MATHS", "Maths", "MATH"),
    SCIENCE("SCIENCE", "Science", "SCI"),
    LITERATURE("LITERATURE", "Literature", "LIT");

    companion object {
        fun fromKey(raw: String?): TriviaCategory =
            entries.firstOrNull { it.key == raw } ?: GEOGRAPHY
    }
}

enum class PointTier(
    val points: Int,
    val penalty: Int
) {
    TIER_10(points = 10, penalty = 5),
    TIER_30(points = 30, penalty = 15),
    TIER_50(points = 50, penalty = 25),
    TIER_100(points = 100, penalty = 50);

    fun nextTier(): PointTier? = when (this) {
        TIER_10 -> TIER_30
        TIER_30 -> TIER_50
        TIER_50 -> TIER_100
        TIER_100 -> null
    }

    companion object {
        fun fromPoints(points: Long): PointTier =
            entries.firstOrNull { it.points.toLong() == points } ?: TIER_10
    }
}

data class TriviaQuestion(
    val id: String,
    val category: TriviaCategory,
    val tier: PointTier,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Keep
@IgnoreExtraProperties
data class UserProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val totalScore: Long = 0L,
    val correctCount: Long = 0L,
    val missedCount: Long = 0L,
    val currentStreak: Long = 0L,
    val bestStreak: Long = 0L,
    val unlockedGeo: Long = 10L,
    val unlockedMaths: Long = 10L,
    val unlockedScience: Long = 10L,
    val unlockedLit: Long = 10L,
    val answeredIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun unlockedTierFor(category: TriviaCategory): PointTier {
        val raw = when (category) {
            TriviaCategory.GEOGRAPHY -> unlockedGeo
            TriviaCategory.MATHS -> unlockedMaths
            TriviaCategory.SCIENCE -> unlockedScience
            TriviaCategory.LITERATURE -> unlockedLit
        }
        return PointTier.fromPoints(raw)
    }

    fun highestTierUnlocked(): PointTier {
        val maxPoints = maxOf(unlockedGeo, unlockedMaths, unlockedScience, unlockedLit)
        return PointTier.fromPoints(maxPoints)
    }
}

@Keep
@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val totalScore: Long = 0L,
    val correctCount: Long = 0L,
    val missedCount: Long = 0L,
    val bestStreak: Long = 0L,
    val highestTier: Long = 10L,
    val visibility: String = "PUBLIC",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AnswerOutcome(
    val isCorrect: Boolean,
    val pointsDelta: Long,
    val newTotalScore: Long,
    val unlockedTier: PointTier?
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
