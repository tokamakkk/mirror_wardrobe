package com.comp7506.mywardrobe.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

data class AuthUser(
    val uid: String,
    val email: String?,
)

sealed class FirebaseAuthResult {
    data class Success(val user: AuthUser) : FirebaseAuthResult()
    data class Error(val message: String) : FirebaseAuthResult()
}

class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    val currentUserFlow: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAuthUser())
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.toAuthUser())
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun currentUser(): AuthUser? = auth.currentUser?.toAuthUser()

    suspend fun register(email: String, password: String): FirebaseAuthResult {
        val normalizedEmail = email.trim()
        if (!normalizedEmail.contains("@")) return FirebaseAuthResult.Error("Invalid email format")
        if (password.length < 6) return FirebaseAuthResult.Error("Password must be at least 6 characters")

        return runCatching {
            val result = withTimeout(AUTH_TIMEOUT_MS) {
                auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
            }
            val user = result.user ?: auth.currentUser
            if (user == null) FirebaseAuthResult.Error("Sign up failed")
            else FirebaseAuthResult.Success(user.toAuthUser())
        }.getOrElse { e ->
            if (e is TimeoutCancellationException) {
                FirebaseAuthResult.Error("Sign up timed out. Check your network and try again")
            } else {
                FirebaseAuthResult.Error(e.message ?: "Sign up failed")
            }
        }
    }

    suspend fun login(email: String, password: String): FirebaseAuthResult {
        val normalizedEmail = email.trim()
        if (!normalizedEmail.contains("@")) return FirebaseAuthResult.Error("Invalid email format")
        if (password.isBlank()) return FirebaseAuthResult.Error("Please enter your password")

        return runCatching {
            val result = withTimeout(AUTH_TIMEOUT_MS) {
                auth.signInWithEmailAndPassword(normalizedEmail, password).await()
            }
            val user = result.user ?: auth.currentUser
            if (user == null) FirebaseAuthResult.Error("Sign in failed")
            else FirebaseAuthResult.Success(user.toAuthUser())
        }.getOrElse { e ->
            if (e is TimeoutCancellationException) {
                FirebaseAuthResult.Error("Sign in timed out. Check your network and try again")
            } else {
                FirebaseAuthResult.Error(e.message ?: "Incorrect email or password")
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    companion object {
        private const val AUTH_TIMEOUT_MS = 12_000L
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(uid = uid, email = email)
