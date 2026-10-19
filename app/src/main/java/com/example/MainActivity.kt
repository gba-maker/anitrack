package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.example.auth.AuthManager
import com.example.data.repository.FirestoreRepository
import com.example.ui.MainScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.AniTrackTheme
import com.example.ui.theme.BackgroundDark
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val credentialManager = CredentialManager.create(this)

        setContent {
            AniTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    AppContent(
                        credentialManager = credentialManager,
                        activity = this@MainActivity
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(
    credentialManager: CredentialManager,
    activity: MainActivity
) {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    // Attempt silent auto-sign-in if no user is signed in yet
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            AuthManager.attemptAutoSignIn(
                context = activity,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = FirebaseAuth.getInstance().currentUser
                },
                onUnauthenticated = {
                    // Stay on AuthScreen
                },
                scope = activity.lifecycleScope
            )
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = FirebaseAuth.getInstance().currentUser
            }
        )
    } else {
        val uid = currentUser!!.uid
        val firestoreRepository = remember(uid) {
            FirestoreRepository(activity)
        }

        MainScreen(
            currentUserId = uid,
            firestoreRepository = firestoreRepository,
            onSignOut = {
                AuthManager.signOut(
                    context = activity,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = activity.lifecycleScope
                )
            }
        )
    }
}
