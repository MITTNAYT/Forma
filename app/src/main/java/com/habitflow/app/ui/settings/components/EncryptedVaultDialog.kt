package com.habitflow.app.ui.settings.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.core.designsystem.FormaTheme
import com.habitflow.app.core.designsystem.component.FormaButton
import com.habitflow.app.core.designsystem.component.FormaButtonStyle

enum class VaultDialogMode {
    ENCRYPT_EXPORT,
    DECRYPT_RESTORE
}

@Composable
fun EncryptedVaultDialog(
    mode: VaultDialogMode,
    onDismiss: () -> Unit,
    onExportWithPassword: (passphrase: String) -> Unit,
    onRestoreWithPassword: (passphrase: String, encryptedPayload: String) -> Unit
) {
    val context = LocalContext.current
    val colors = FormaTheme.colors

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var encryptedPayload by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isExport = mode == VaultDialogMode.ENCRYPT_EXPORT

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = colors.background,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExport) Icons.Rounded.Bookmark else Icons.Rounded.TrackChanges,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isExport) "Zero-Knowledge Vault Export" else "Restore Encrypted Vault",
                        style = FormaTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "AES-256-GCM • PBKDF2-SHA256",
                        style = FormaTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isExport) {
                            "Your habits, reflections, and metrics are encrypted on-device with your chosen passphrase. If you forget this passphrase, data cannot be recovered."
                        } else {
                            "Paste the encrypted envelope or vault string below, then enter your master passphrase to safely unpack your records."
                        },
                        style = FormaTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                if (!isExport) {
                    // Ciphertext Payload Input
                    OutlinedTextField(
                        value = encryptedPayload,
                        onValueChange = {
                            encryptedPayload = it
                            errorMessage = null
                        },
                        label = { Text("Encrypted Vault Envelope (.habitvault)", fontSize = 12.sp) },
                        placeholder = { Text("--- HABITFLOW ENCRYPTED VAULT V1 --- ...", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        textStyle = FormaTheme.typography.bodySmall.copy(
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        maxLines = 4
                    )
                }

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text(if (isExport) "Create Master Passphrase" else "Master Passphrase", fontSize = 12.sp) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isExport) ImeAction.Next else ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Text(
                                text = if (isPasswordVisible) "Hide" else "Show",
                                style = FormaTheme.typography.labelSmall,
                                color = colors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = FormaTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.border,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    )
                )

                if (isExport) {
                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Master Passphrase", fontSize = 12.sp) },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = FormaTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.border,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        )
                    )
                }

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        style = FormaTheme.typography.bodySmall,
                        color = Color(0xFFC0392B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            FormaButton(
                text = if (isProcessing) "Encrypting..." else if (isExport) "Export Encrypted Vault" else "Decrypt & Restore",
                onClick = {
                    if (password.length < 6) {
                        errorMessage = "Passphrase must be at least 6 characters."
                        return@FormaButton
                    }
                    if (isExport && password != confirmPassword) {
                        errorMessage = "Passphrases do not match."
                        return@FormaButton
                    }
                    if (!isExport && encryptedPayload.isBlank()) {
                        errorMessage = "Please provide the encrypted vault text."
                        return@FormaButton
                    }

                    isProcessing = true
                    if (isExport) {
                        onExportWithPassword(password)
                    } else {
                        onRestoreWithPassword(password, encryptedPayload)
                    }
                },
                style = FormaButtonStyle.PRIMARY,
                enabled = !isProcessing && password.isNotBlank()
            )
        },
        dismissButton = {
            FormaButton(
                text = "Cancel",
                onClick = onDismiss,
                style = FormaButtonStyle.OUTLINE,
                enabled = !isProcessing
            )
        }
    )
}
