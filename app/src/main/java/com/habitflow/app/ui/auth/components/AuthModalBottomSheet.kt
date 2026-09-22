package com.habitflow.app.ui.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.component.FormaButton
import com.habitflow.app.core.designsystem.component.FormaButtonStyle
import com.habitflow.app.core.designsystem.component.FormaEmblem
import com.habitflow.app.core.designsystem.motion.formaPressEffect
import com.habitflow.app.ui.auth.AuthViewModel

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthModalBottomSheet(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val colors = FormaTheme.colors
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isLoading by viewModel.isLoading.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    var mode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Emblem & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FormaEmblem(size = 32.dp, animated = false)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SYNC SANCTUARY",
                        style = FormaTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Headline & Subtitle
            Text(
                text = when (mode) {
                    AuthMode.SIGN_IN -> "Welcome Back"
                    AuthMode.SIGN_UP -> "Create Account"
                    AuthMode.FORGOT_PASSWORD -> "Reset Password"
                },
                style = FormaTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (mode) {
                    AuthMode.SIGN_IN -> "Sign in to keep your rituals and reflection history synced across devices."
                    AuthMode.SIGN_UP -> "Back up your personal sanctuary seamlessly without compromising privacy."
                    AuthMode.FORGOT_PASSWORD -> "Enter your email to receive a secure password reset link."
                },
                style = FormaTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Selector Pill (Sign In vs Sign Up)
            if (mode != AuthMode.FORGOT_PASSWORD) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (mode == AuthMode.SIGN_IN) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
                            .formaPressEffect(targetScale = 0.98f) { mode = AuthMode.SIGN_IN }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            style = FormaTheme.typography.labelMedium,
                            fontWeight = if (mode == AuthMode.SIGN_IN) FontWeight.Bold else FontWeight.Medium,
                            color = if (mode == AuthMode.SIGN_IN) colors.textPrimary else colors.textSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (mode == AuthMode.SIGN_UP) colors.surface else androidx.compose.ui.graphics.Color.Transparent)
                            .formaPressEffect(targetScale = 0.98f) { mode = AuthMode.SIGN_UP }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "New Account",
                            style = FormaTheme.typography.labelMedium,
                            fontWeight = if (mode == AuthMode.SIGN_UP) FontWeight.Bold else FontWeight.Medium,
                            color = if (mode == AuthMode.SIGN_UP) colors.textPrimary else colors.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Status / Error message banner
            AnimatedVisibility(visible = uiMessage != null) {
                uiMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentSoft)
                            .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = msg,
                            style = FormaTheme.typography.bodySmall,
                            color = colors.accent,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Input Fields
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (mode == AuthMode.SIGN_UP) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = { Text("Your Name (optional)", color = colors.textTertiary, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email Address", color = colors.textTertiary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = if (mode == AuthMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (mode != AuthMode.FORGOT_PASSWORD) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password (min 6 characters)", color = colors.textTertiary, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (mode == AuthMode.SIGN_IN) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { mode = AuthMode.FORGOT_PASSWORD }) {
                        Text(
                            text = "Forgot password?",
                            style = FormaTheme.typography.bodySmall,
                            color = colors.accent,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Primary Action Button
            FormaButton(
                text = when {
                    isLoading -> "Syncing..."
                    mode == AuthMode.SIGN_IN -> "Sign In with Email"
                    mode == AuthMode.SIGN_UP -> "Create Sanctuary Account"
                    mode == AuthMode.FORGOT_PASSWORD -> "Send Reset Link"
                    else -> "Continue"
                },
                onClick = {
                    focusManager.clearFocus()
                    when (mode) {
                        AuthMode.SIGN_IN -> viewModel.signInWithEmail(email, password, onSuccess = onDismiss)
                        AuthMode.SIGN_UP -> viewModel.signUpWithEmail(email, password, displayName, onSuccess = onDismiss)
                        AuthMode.FORGOT_PASSWORD -> viewModel.resetPassword(email)
                    }
                },
                style = FormaButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            // Or Google 1-Tap
            if (mode != AuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(colors.border))
                    Text(
                        text = "OR",
                        style = FormaTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(colors.border))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .formaPressEffect(targetScale = 0.97f) {
                            viewModel.launchGoogleSignIn(context)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Google Sign In",
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            style = FormaTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (mode == AuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { mode = AuthMode.SIGN_IN }) {
                    Text("← Back to Sign In", color = colors.textSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}
