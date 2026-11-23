package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectsPlayer
import com.example.data.GameRepository
import com.example.data.TriviaQuestionBank
import com.example.model.AnswerOutcome
import com.example.model.LeaderboardEntry
import com.example.model.PointTier
import com.example.model.TriviaCategory
import com.example.model.TriviaQuestion
import com.example.model.UiState
import com.example.model.UserProgress
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameSessionState(
    val activeQuestion: TriviaQuestion? = null,
    val selectedOptionIndex: Int? = null,
    val answerOutcome: AnswerOutcome? = null,
    val isSubmittingAnswer: Boolean = false,
    val actionError: String? = null,
    val isCreatingProfile: Boolean = false,
    val profileError: String? = null,
    val isUpdatingUsername: Boolean = false,
    val transientNotice: String? = null
)

class GameViewModel(
    private val repository: GameRepository,
    private val currentUserId: String,
    private val soundEffects: SoundEffectsPlayer
) : ViewModel() {

    val isMuted: StateFlow<Boolean> = soundEffects.isMuted

    val userProgressState: StateFlow<UiState<UserProgress?>> = repository.observeUserProgress(currentUserId)
        .map<UserProgress?, UiState<UserProgress?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user progress", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load player profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val currentScore: StateFlow<Long> = userProgressState
        .map { state -> (state as? UiState.Success)?.data?.totalScore ?: 0L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = 0L
        )

    val unlockedTiers: StateFlow<Map<TriviaCategory, PointTier>> = userProgressState
        .map { state ->
            val progress = (state as? UiState.Success)?.data
            TriviaCategory.entries.associateWith { category ->
                progress?.unlockedTierFor(category) ?: PointTier.TIER_10
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TriviaCategory.entries.associateWith { PointTier.TIER_10 }
        )

    val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>> = repository.observeLeaderboard()
        .map<List<LeaderboardEntry>, UiState<List<LeaderboardEntry>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing leaderboard", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Failed to load leaderboard"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _sessionState = MutableStateFlow(GameSessionState())
    val sessionState: StateFlow<GameSessionState> = _sessionState.asStateFlow()

    private fun currentProgressOrNull(): UserProgress? =
        (userProgressState.value as? UiState.Success)?.data

    fun isTierUnlocked(category: TriviaCategory, tier: PointTier): Boolean {
        val unlocked = currentProgressOrNull()?.unlockedTierFor(category) ?: PointTier.TIER_10
        return tier.points <= unlocked.points
    }

    fun toggleMute() {
        soundEffects.toggleMute()
    }

    fun clearNotice() {
        _sessionState.update { it.copy(transientNotice = null, actionError = null) }
    }

    fun createProfile(usernameInput: String) {
        val cleaned = GameRepository.sanitizeUsername(usernameInput)
        if (cleaned.length < 2) {
            _sessionState.update {
                it.copy(profileError = "Please enter at least 2 characters for your leaderboard name.")
            }
            return
        }
        viewModelScope.launch {
            _sessionState.update { it.copy(isCreatingProfile = true, profileError = null) }
            val result = repository.createProfile(cleaned)
            result.fold(
                onSuccess = {
                    soundEffects.playCorrect()
                    _sessionState.update { it.copy(isCreatingProfile = false, profileError = null) }
                },
                onFailure = { error ->
                    _sessionState.update {
                        it.copy(
                            isCreatingProfile = false,
                            profileError = error.localizedMessage ?: "Unable to create profile."
                        )
                    }
                }
            )
        }
    }

    fun updateUsername(newUsername: String) {
        val cleaned = GameRepository.sanitizeUsername(newUsername)
        if (cleaned.length < 2) {
            _sessionState.update {
                it.copy(transientNotice = "Username must be at least 2 characters.")
            }
            return
        }
        viewModelScope.launch {
            _sessionState.update { it.copy(isUpdatingUsername = true, transientNotice = null) }
            val result = repository.updateUsername(cleaned)
            result.fold(
                onSuccess = {
                    _sessionState.update {
                        it.copy(isUpdatingUsername = false, transientNotice = "Username updated to $cleaned")
                    }
                },
                onFailure = { error ->
                    _sessionState.update {
                        it.copy(
                            isUpdatingUsername = false,
                            transientNotice = error.localizedMessage ?: "Failed to update username"
                        )
                    }
                }
            )
        }
    }

    fun onTileClicked(category: TriviaCategory, tier: PointTier) {
        val progress = currentProgressOrNull() ?: return
        val unlockedTier = progress.unlockedTierFor(category)
        if (tier.points > unlockedTier.points) {
            val requiredTier = when (tier) {
                PointTier.TIER_30 -> PointTier.TIER_10
                PointTier.TIER_50 -> PointTier.TIER_30
                PointTier.TIER_100 -> PointTier.TIER_50
                PointTier.TIER_10 -> PointTier.TIER_10
            }
            _sessionState.update {
                it.copy(
                    transientNotice = "Answer a ${requiredTier.points} pt ${category.label} clue to unlock ${tier.points} pts."
                )
            }
            return
        }

        soundEffects.playSelect()
        val question = TriviaQuestionBank.pickQuestion(
            category = category,
            tier = tier,
            answeredIds = progress.answeredIds
        )
        _sessionState.update {
            it.copy(
                activeQuestion = question,
                selectedOptionIndex = null,
                answerOutcome = null,
                isSubmittingAnswer = false,
                actionError = null,
                transientNotice = null
            )
        }
    }

    fun submitAnswer(optionIndex: Int) {
        val progress = currentProgressOrNull() ?: return
        val currentState = _sessionState.value
        val question = currentState.activeQuestion ?: return
        if (currentState.selectedOptionIndex != null || currentState.isSubmittingAnswer) return
        if (optionIndex !in question.options.indices) return

        val isCorrect = validateAnswer(question, optionIndex)
        val willUnlockTier = isCorrect &&
            question.tier == progress.unlockedTierFor(question.category) &&
            question.tier.nextTier() != null

        // Trigger distinct MediaPlayer SFX for correct, tier-unlock, or incorrect answers
        if (willUnlockTier) {
            soundEffects.playTierUnlock()
        } else if (isCorrect) {
            soundEffects.playCorrect()
        } else {
            soundEffects.playIncorrect()
        }

        _sessionState.update {
            it.copy(
                selectedOptionIndex = optionIndex,
                isSubmittingAnswer = true,
                actionError = null
            )
        }

        viewModelScope.launch {
            val result = repository.recordAnswer(progress, question, isCorrect)
            result.fold(
                onSuccess = { outcome ->
                    _sessionState.update {
                        it.copy(
                            isSubmittingAnswer = false,
                            answerOutcome = outcome
                        )
                    }
                },
                onFailure = { error ->
                    val fallbackDelta = if (isCorrect) question.tier.points.toLong() else -question.tier.penalty.toLong()
                    _sessionState.update {
                        it.copy(
                            isSubmittingAnswer = false,
                            answerOutcome = AnswerOutcome(
                                isCorrect = isCorrect,
                                pointsDelta = fallbackDelta,
                                newTotalScore = maxOf(0L, progress.totalScore + fallbackDelta),
                                unlockedTier = null
                            ),
                            actionError = error.localizedMessage ?: "Failed to sync score to cloud"
                        )
                    }
                }
            )
        }
    }

    fun dismissQuestionDialog() {
        _sessionState.update {
            it.copy(
                activeQuestion = null,
                selectedOptionIndex = null,
                answerOutcome = null,
                isSubmittingAnswer = false,
                actionError = null
            )
        }
    }

    fun startNewRound() {
        val progress = currentProgressOrNull() ?: return
        viewModelScope.launch {
            val result = repository.resetBoardCycle(progress)
            result.fold(
                onSuccess = {
                    _sessionState.update {
                        it.copy(transientNotice = "Fresh round ready. Your score & unlocked tiers remain.")
                    }
                },
                onFailure = { error ->
                    _sessionState.update {
                        it.copy(transientNotice = error.localizedMessage ?: "Unable to reset board round")
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEffects.release()
    }

    companion object {
        private const val TAG = "GameViewModel"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun validateAnswer(question: TriviaQuestion, selectedOptionIndex: Int): Boolean =
            selectedOptionIndex in question.options.indices &&
                selectedOptionIndex == question.correctIndex
    }
}
