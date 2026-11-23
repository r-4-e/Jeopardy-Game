package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.audio.MediaPlayerSoundEffectsManager
import com.example.auth.signOut
import com.example.data.GameRepository
import com.example.model.UiState
import com.example.model.UserProgress
import com.example.ui.screens.AccountDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.JeopardyBoardScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OnboardingProfileScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_BOARD = "board"
const val ROUTE_LEADERBOARD = "leaderboard"

@Composable
fun ClueGridApp() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        key(user.uid) {
            AuthenticatedGameRoot(
                currentUserId = user.uid,
                suggestedDisplayName = user.displayName.orEmpty(),
                onSignedOut = {
                    currentUser = null
                }
            )
        }
    }
}

@Composable
private fun AuthenticatedGameRoot(
    currentUserId: String,
    suggestedDisplayName: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: GameViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                GameViewModel(
                    repository = GameRepository(db),
                    currentUserId = currentUserId,
                    soundEffects = MediaPlayerSoundEffectsManager(app.applicationContext)
                )
            }
        }
    )

    val progressState by viewModel.userProgressState.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()

    val performSignOut: () -> Unit = {
        signOut(
            credentialManager = credentialManager,
            scope = scope,
            onSignOutComplete = onSignedOut
        )
    }

    when (val state = progressState) {
        is UiState.Loading -> {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        is UiState.Error -> {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        is UiState.Success -> {
            val progress = state.data
            if (progress == null) {
                OnboardingProfileScreen(
                    suggestedName = suggestedDisplayName,
                    isSubmitting = sessionState.isCreatingProfile,
                    errorMessage = sessionState.profileError,
                    onCreateProfile = viewModel::createProfile,
                    onSignOutClick = performSignOut
                )
            } else {
                MainGameShell(
                    progress = progress,
                    leaderboardState = leaderboardState,
                    sessionState = sessionState,
                    isMuted = isMuted,
                    currentUserId = currentUserId,
                    viewModel = viewModel,
                    onSignOut = performSignOut
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainGameShell(
    progress: UserProgress,
    leaderboardState: UiState<List<com.example.model.LeaderboardEntry>>,
    sessionState: GameSessionState,
    isMuted: Boolean,
    currentUserId: String,
    viewModel: GameViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_BOARD
    var showAccountDialog by remember { mutableStateOf(false) }

    val navigateTo: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = viewModel::toggleMute,
                            modifier = Modifier.testTag("mute_sfx_button")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                                contentDescription = if (isMuted) {
                                    stringResource(R.string.sound_off)
                                } else {
                                    stringResource(R.string.sound_on)
                                }
                            )
                        }

                        Surface(
                            onClick = { showAccountDialog = true },
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("account_chip_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = stringResource(R.string.account_settings),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = progress.username,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_BOARD,
                            onClick = { navigateTo(ROUTE_BOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_BOARD) {
                                        Icons.Filled.GridView
                                    } else {
                                        Icons.Outlined.GridView
                                    },
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_board)) },
                            modifier = Modifier.testTag("nav_board")
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_LEADERBOARD,
                            onClick = { navigateTo(ROUTE_LEADERBOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_LEADERBOARD) {
                                        Icons.Filled.EmojiEvents
                                    } else {
                                        Icons.Outlined.EmojiEvents
                                    },
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_leaderboard)) },
                            modifier = Modifier.testTag("nav_leaderboard")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.size(12.dp))
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_BOARD,
                            onClick = { navigateTo(ROUTE_BOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_BOARD) {
                                        Icons.Filled.GridView
                                    } else {
                                        Icons.Outlined.GridView
                                    },
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_board)) },
                            modifier = Modifier.testTag("nav_board")
                        )
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_LEADERBOARD,
                            onClick = { navigateTo(ROUTE_LEADERBOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == ROUTE_LEADERBOARD) {
                                        Icons.Filled.EmojiEvents
                                    } else {
                                        Icons.Outlined.EmojiEvents
                                    },
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(R.string.nav_leaderboard)) },
                            modifier = Modifier.testTag("nav_leaderboard")
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_BOARD,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_BOARD) {
                        JeopardyBoardScreen(
                            progress = progress,
                            sessionState = sessionState,
                            onTileClick = viewModel::onTileClicked,
                            onOptionSelected = viewModel::submitAnswer,
                            onDismissDialog = viewModel::dismissQuestionDialog,
                            onStartNewRound = viewModel::startNewRound,
                            onDismissNotice = viewModel::clearNotice
                        )
                    }

                    composable(ROUTE_LEADERBOARD) {
                        LeaderboardScreen(
                            leaderboardState = leaderboardState,
                            currentUserId = currentUserId
                        )
                    }
                }
            }
        }
    }

    if (showAccountDialog) {
        AccountDialog(
            progress = progress,
            isMuted = isMuted,
            isUpdatingUsername = sessionState.isUpdatingUsername,
            onUpdateUsername = viewModel::updateUsername,
            onToggleMute = viewModel::toggleMute,
            onSignOut = {
                showAccountDialog = false
                onSignOut()
            },
            onDismiss = { showAccountDialog = false }
        )
    }
}
