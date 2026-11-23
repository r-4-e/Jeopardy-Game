package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AnswerOutcome
import com.example.model.LeaderboardEntry
import com.example.model.PointTier
import com.example.model.TriviaCategory
import com.example.model.TriviaQuestion
import com.example.model.UserProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class GameRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProgress(userId: String): Flow<UserProgress?> = flow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank() || uid != userId) {
            throw IllegalStateException("Authenticated user mismatch.")
        }
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProgress::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = flow {
        val uid = requireUserId()
        val path = "leaderboard"
        val query = db.collection(path)
            .whereEqualTo("visibility", "PUBLIC")
            .limit(100)
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        LeaderboardEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<LeaderboardEntry> { it.totalScore }
                            .thenByDescending { it.correctCount }
                            .thenBy { it.username.lowercase() }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createProfile(rawUsername: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in with Google."))
        val userPath = "users/$uid"
        return try {
            val username = sanitizeUsername(rawUsername)
            require(username.length in 2..24) { "Username must be between 2 and 24 characters." }

            val userRef = db.collection("users").document(uid)
            val leaderboardRef = db.collection("leaderboard").document(uid)

            val userPayload = mapOf(
                "userId" to uid,
                "username" to username,
                "totalScore" to 0L,
                "correctCount" to 0L,
                "missedCount" to 0L,
                "currentStreak" to 0L,
                "bestStreak" to 0L,
                "unlockedGeo" to 10L,
                "unlockedMaths" to 10L,
                "unlockedScience" to 10L,
                "unlockedLit" to 10L,
                "answeredIds" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val leaderboardPayload = mapOf(
                "userId" to uid,
                "username" to username,
                "totalScore" to 0L,
                "correctCount" to 0L,
                "missedCount" to 0L,
                "bestStreak" to 0L,
                "highestTier" to 10L,
                "visibility" to "PUBLIC",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            db.batch().apply {
                set(userRef, userPayload)
                set(leaderboardRef, leaderboardPayload)
            }.commit().await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun updateUsername(rawUsername: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in with Google."))
        val userPath = "users/$uid"
        return try {
            val username = sanitizeUsername(rawUsername)
            require(username.length in 2..24) { "Username must be between 2 and 24 characters." }

            val userRef = db.collection("users").document(uid)
            val leaderboardRef = db.collection("leaderboard").document(uid)

            db.batch().apply {
                update(
                    userRef,
                    mapOf(
                        "username" to username,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                update(
                    leaderboardRef,
                    mapOf(
                        "username" to username,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.commit().await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun recordAnswer(
        current: UserProgress,
        question: TriviaQuestion,
        isCorrect: Boolean
    ): Result<AnswerOutcome> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in with Google."))
        val userPath = "users/$uid"
        return try {
            check(uid == current.userId) { "Signed-in user mismatch." }

            val delta = if (isCorrect) question.tier.points.toLong() else -question.tier.penalty.toLong()
            val newScore = maxOf(0L, current.totalScore + delta)
            val newCorrect = if (isCorrect) current.correctCount + 1L else current.correctCount
            val newMissed = if (isCorrect) current.missedCount else current.missedCount + 1L
            val newCurrentStreak = if (isCorrect) current.currentStreak + 1L else 0L
            val newBestStreak = maxOf(current.bestStreak, newCurrentStreak)

            val currentCategoryTier = current.unlockedTierFor(question.category)
            val nextTier = if (
                isCorrect &&
                question.tier == currentCategoryTier &&
                question.tier.nextTier() != null
            ) {
                question.tier.nextTier()
            } else {
                null
            }
            val updatedCategoryTierPoints = (nextTier?.points ?: currentCategoryTier.points).toLong()

            val newGeo = if (question.category == TriviaCategory.GEOGRAPHY) updatedCategoryTierPoints else current.unlockedGeo
            val newMaths = if (question.category == TriviaCategory.MATHS) updatedCategoryTierPoints else current.unlockedMaths
            val newScience = if (question.category == TriviaCategory.SCIENCE) updatedCategoryTierPoints else current.unlockedScience
            val newLit = if (question.category == TriviaCategory.LITERATURE) updatedCategoryTierPoints else current.unlockedLit
            val highestTier = maxOf(newGeo, newMaths, newScience, newLit)

            val updatedAnswered = (current.answeredIds + question.id)
                .distinct()
                .takeLast(48)

            val userRef = db.collection("users").document(uid)
            val leaderboardRef = db.collection("leaderboard").document(uid)

            db.batch().apply {
                update(
                    userRef,
                    mapOf(
                        "totalScore" to newScore,
                        "correctCount" to newCorrect,
                        "missedCount" to newMissed,
                        "currentStreak" to newCurrentStreak,
                        "bestStreak" to newBestStreak,
                        "unlockedGeo" to newGeo,
                        "unlockedMaths" to newMaths,
                        "unlockedScience" to newScience,
                        "unlockedLit" to newLit,
                        "answeredIds" to updatedAnswered,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                update(
                    leaderboardRef,
                    mapOf(
                        "totalScore" to newScore,
                        "correctCount" to newCorrect,
                        "missedCount" to newMissed,
                        "bestStreak" to newBestStreak,
                        "highestTier" to highestTier,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.commit().await()

            Result.success(
                AnswerOutcome(
                    isCorrect = isCorrect,
                    pointsDelta = delta,
                    newTotalScore = newScore,
                    unlockedTier = nextTier
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun resetBoardCycle(current: UserProgress): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User must be signed in with Google."))
        val userPath = "users/$uid"
        return try {
            check(uid == current.userId) { "Signed-in user mismatch." }
            db.collection("users").document(uid)
                .update(
                    mapOf(
                        "answeredIds" to emptyList<String>(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userPath)
            Result.failure(e)
        }
    }

    companion object {
        fun sanitizeUsername(raw: String): String =
            raw.trim().replace(Regex("\\s+"), " ").take(24)
    }
}
