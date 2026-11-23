package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.TriviaQuestionBank
import com.example.model.AnswerOutcome
import com.example.model.PointTier
import com.example.model.TriviaCategory
import com.example.model.TriviaQuestion
import com.example.model.UserProgress
import com.example.ui.GameSessionState

@Composable
fun JeopardyBoardScreen(
    progress: UserProgress,
    sessionState: GameSessionState,
    onTileClick: (TriviaCategory, PointTier) -> Unit,
    onOptionSelected: (Int) -> Unit,
    onDismissDialog: () -> Unit,
    onStartNewRound: () -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val compactHeaders = maxWidth < 400.dp

        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Minimal Score & Progression Strip
            ScoreSummaryStrip(progress = progress)

            // Transient notice banner (e.g., locked tier hint or round reset)
            AnimatedVisibility(
                visible = sessionState.transientNotice != null,
                enter = fadeIn(animationSpec = spring()),
                exit = fadeOut(animationSpec = spring())
            ) {
                Surface(
                    onClick = onDismissNotice,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = sessionState.transientNotice.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Dismiss notice",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Category Headers Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TriviaCategory.entries.forEach { category ->
                    CategoryHeaderChip(
                        category = category,
                        unlockedTier = progress.unlockedTierFor(category),
                        compact = compactHeaders,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4x4 Tiered Jeopardy Grid (10, 30, 50, 100)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PointTier.entries.forEach { tier ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TriviaCategory.entries.forEach { category ->
                            val unlockedTier = progress.unlockedTierFor(category)
                            val isUnlocked = tier.points <= unlockedTier.points
                            val isFrontier = tier == unlockedTier
                            val answeredInCell = TriviaQuestionBank.questions.count {
                                it.category == category && it.tier == tier && it.id in progress.answeredIds
                            }

                            BoardTile(
                                category = category,
                                tier = tier,
                                isUnlocked = isUnlocked,
                                isFrontier = isFrontier,
                                answeredCount = answeredInCell,
                                onClick = { onTileClick(category, tier) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Scaling Stakes Legend + New Round Reset
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Tier Progression & Scaled Risk",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Solve a tier to unlock the next. Missed shots deduct half (−5, −15, −25, −50).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (progress.answeredIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(12.dp))
                        OutlinedButton(
                            onClick = onStartNewRound,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("new_round_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = stringResource(R.string.new_round),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }

    val activeQuestion = sessionState.activeQuestion
    if (activeQuestion != null) {
        ClueDialog(
            question = activeQuestion,
            selectedOptionIndex = sessionState.selectedOptionIndex,
            outcome = sessionState.answerOutcome,
            isSubmitting = sessionState.isSubmittingAnswer,
            syncError = sessionState.actionError,
            onSelectOption = onOptionSelected,
            onContinue = onDismissDialog
        )
    }
}

@Composable
private fun ScoreSummaryStrip(progress: UserProgress) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TOTAL SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = progress.totalScore.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (progress.currentStreak > 0) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${progress.currentStreak}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ACCURACY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val totalAttempts = progress.correctCount + progress.missedCount
                    val pct = if (totalAttempts > 0) {
                        ((progress.correctCount * 100) / totalAttempts).toInt()
                    } else {
                        0
                    }
                    Text(
                        text = if (totalAttempts > 0) "$pct%" else "—",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryHeaderChip(
    category: TriviaCategory,
    unlockedTier: PointTier,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = category.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (compact) category.shortLabel else category.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = "MAX ${unlockedTier.points}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BoardTile(
    category: TriviaCategory,
    tier: PointTier,
    isUnlocked: Boolean,
    isFrontier: Boolean,
    answeredCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighTier = tier == PointTier.TIER_100
    val containerColor = when {
        !isUnlocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        isFrontier && isHighTier -> MaterialTheme.colorScheme.secondaryContainer
        isFrontier -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        !isUnlocked -> MaterialTheme.colorScheme.outlineVariant
        isFrontier && isHighTier -> MaterialTheme.colorScheme.secondary
        isFrontier -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    val contentColor = when {
        !isUnlocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        isFrontier && isHighTier -> MaterialTheme.colorScheme.onSecondaryContainer
        isFrontier -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isFrontier) 1.5.dp else 1.dp, borderColor),
        modifier = modifier
            .heightIn(min = 82.dp)
            .minimumInteractiveComponentSize()
            .testTag("tile_${category.key}_${tier.points}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Locked tier",
                    tint = contentColor,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.TopEnd)
                )
            } else if (answeredCount > 0) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Completed clue",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${tier.points}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = if (isUnlocked) "−${tier.penalty} miss" else "Locked",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ClueDialog(
    question: TriviaQuestion,
    selectedOptionIndex: Int?,
    outcome: AnswerOutcome?,
    isSubmitting: Boolean,
    syncError: String?,
    onSelectOption: (Int) -> Unit,
    onContinue: () -> Unit
) {
    val hasAnswered = selectedOptionIndex != null

    Dialog(
        onDismissRequest = {
            if (hasAnswered && !isSubmitting) onContinue()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = hasAnswered,
            dismissOnClickOutside = hasAnswered
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 6.dp,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 520.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Category + Point Stakes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon(question.category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = question.category.label.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "+${question.tier.points} / −${question.tier.penalty} PTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Clue Prompt
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Answer Options
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.options.forEachIndexed { index, optionText ->
                        val isSelected = selectedOptionIndex == index
                        val isCorrectOption = index == question.correctIndex

                        val targetBg = when {
                            !hasAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            isCorrectOption -> MaterialTheme.colorScheme.tertiaryContainer
                            isSelected -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                        val targetBorder = when {
                            !hasAnswered -> MaterialTheme.colorScheme.outlineVariant
                            isCorrectOption -> MaterialTheme.colorScheme.tertiary
                            isSelected -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                        val targetText = when {
                            !hasAnswered -> MaterialTheme.colorScheme.onSurface
                            isCorrectOption -> MaterialTheme.colorScheme.onTertiaryContainer
                            isSelected -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        val animatedBg by animateColorAsState(targetValue = targetBg, label = "optBg")

                        Surface(
                            onClick = {
                                if (!hasAnswered) onSelectOption(index)
                            },
                            enabled = !hasAnswered,
                            shape = RoundedCornerShape(14.dp),
                            color = animatedBg,
                            border = BorderStroke(1.2.dp, targetBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("option_$index")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = targetText,
                                    modifier = Modifier.weight(1f)
                                )
                                if (hasAnswered && isCorrectOption) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Correct option",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (hasAnswered && isSelected && !isCorrectOption) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Missed option",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Outcome & Explanation Feedback
                AnimatedVisibility(visible = hasAnswered) {
                    val wasCorrect = selectedOptionIndex == question.correctIndex
                    val pointsDelta = outcome?.pointsDelta
                        ?: if (wasCorrect) question.tier.points.toLong() else -question.tier.penalty.toLong()
                    val deltaLabel = if (pointsDelta >= 0) "+$pointsDelta pts" else "$pointsDelta pts"

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (wasCorrect) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (wasCorrect) "Correct!" else "Missed Shot",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (wasCorrect) {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        }
                                    )
                                    Text(
                                        text = deltaLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (wasCorrect) {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        }
                                    )
                                }

                                Text(
                                    text = question.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (wasCorrect) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )

                                if (outcome?.unlockedTier != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AutoAwesome,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "${question.category.label} ${outcome.unlockedTier.points}-pt tier unlocked!",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (syncError != null) {
                            Text(
                                text = syncError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = onContinue,
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("continue_button")
                        ) {
                            Text(
                                text = if (isSubmitting) "Saving score..." else "Back to Board",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

fun categoryIcon(category: TriviaCategory): ImageVector = when (category) {
    TriviaCategory.GEOGRAPHY -> Icons.Outlined.Public
    TriviaCategory.MATHS -> Icons.Outlined.Functions
    TriviaCategory.SCIENCE -> Icons.Outlined.Science
    TriviaCategory.LITERATURE -> Icons.AutoMirrored.Outlined.MenuBook
}
