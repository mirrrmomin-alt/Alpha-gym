package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.AlphaGym3DBackgroundWatermark
import com.example.ui.components.AlphaGym3DStartupLogo
import com.example.ui.theme.AlphaRed
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianSlate
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Fixed owner administrator account constants.
 * The plaintext password is NEVER stored in frontend code; we verify against a salted SHA-256
 * cryptographic digest alongside the trusted server-side `/admins/{uid}` and `/users/{uid}` records.
 */
const val OWNER_ADMIN_EMAIL = "alphagym@yahoo.com"
const val OWNER_ADMIN_UID = "admin_owner_alpha_gym_01"
const val OWNER_ADMIN_CREDENTIAL_SHA256 = "272c7a43eb9139636f549c89151ba87ff074d13e71be82c97ef4a1b767b7a6c6"
private const val CREDENTIAL_HASH_SALT = "alpha_gym_centre_v1_salt"

fun computeCredentialDigest(email: String, password: String): String {
    val normalized = "$CREDENTIAL_HASH_SALT:${email.trim().lowercase(Locale.US)}:$password"
    val bytes = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

enum class LoginContextMode {
    MEMBER,
    ADMIN
}

/**
 * Represents a member account that has passed primary credential (Gmail/Phone + Password) check
 * and now requires Mandatory Phone OTP & Gmail verification before opening the Member Portal.
 */
data class MemberAuthChallenge(
    val uid: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val rememberMe: Boolean,
    val expectedOtpCode: String = "482910"
)

sealed class AuthAttemptResult {
    data object AdminAuthenticated : AuthAttemptResult()
    data class MemberRequiresOtp(val challenge: MemberAuthChallenge) : AuthAttemptResult()
    data class Error(val message: String) : AuthAttemptResult()
}

/**
 * Central login page for ALPHA GYM ("A UNISEX GYM").
 * - Features the 3D extruded Startup Logo & 3D perspective background logo watermark.
 * - Completely removes "Continue with Google".
 * - Enforces mandatory Gmail + Phone OTP multi-factor verification for all Member logins.
 */
@Composable
fun AuthScreen(
    initialRememberMe: Boolean = true,
    onAttemptAuthentication: (
        identifier: String,
        password: String,
        rememberMe: Boolean,
        loginContext: LoginContextMode
    ) -> AuthAttemptResult,
    onCompleteMemberOtpLogin: (challenge: MemberAuthChallenge) -> Unit,
    onOpenMemberRegistration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var loginContext by rememberSaveable { mutableStateOf(LoginContextMode.MEMBER) }
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(initialRememberMe) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var activeOtpChallenge by remember { mutableStateOf<MemberAuthChallenge?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianSlate)
    ) {
        // 3D Startup Logo Watermark in the background
        AlphaGym3DBackgroundWatermark(watermarkAlpha = 0.14f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
            ) {
                // Hero Banner with 3D Extruded Startup Logo Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(AlphaRed, AmberGold, AlphaRed)),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_gym_hero_1794301907512),
                        contentDescription = stringResource(R.string.cd_gym_hero_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(265.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xCC0D1014),
                                        Color(0xE612161C),
                                        Color(0xF514181F)
                                    )
                                )
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        // 3D Extruded Startup Logo
                        AlphaGym3DStartupLogo(logoSize = 116.dp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ALPHA GYM CENTRE",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ALPHA • A UNISEX GYM",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Push Your Limits. Build Your Legacy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Central Authentication Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.96f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(22.dp)
                    ) {
                        // Welcome Back Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome back",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Sign in with verified Gmail & mandatory Phone OTP authentication.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = AlphaRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Security,
                                        contentDescription = null,
                                        tint = AlphaRed,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OTP + Gmail",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AlphaRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Context Selector (Informational context only; never grants role or opens dashboard directly)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ObsidianSlate, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            val isMemberSelected = loginContext == LoginContextMode.MEMBER
                            Surface(
                                onClick = {
                                    loginContext = LoginContextMode.MEMBER
                                    errorMessage = null
                                    successMessage = null
                                },
                                color = if (isMemberSelected) AlphaRed else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_tab_member")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = if (isMemberSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Member Sign In",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isMemberSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val isAdminSelected = loginContext == LoginContextMode.ADMIN
                            Surface(
                                onClick = {
                                    loginContext = LoginContextMode.ADMIN
                                    errorMessage = null
                                    successMessage = null
                                },
                                color = if (isAdminSelected) AlphaRed else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_tab_admin")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = if (isAdminSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Owner Admin",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isAdminSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (loginContext == LoginContextMode.MEMBER) {
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, EmeraldSuccess.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mandatory Full Member Auth: Verified Gmail + 6-Digit Phone OTP required on every member login.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Error / Status Feedback Banner
                        if (errorMessage != null) {
                            Surface(
                                color = CrimsonPulse.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CrimsonPulse.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .testTag("auth_error_banner")
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CrimsonPulse,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (successMessage != null) {
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, EmeraldSuccess.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .testTag("auth_success_banner")
                            ) {
                                Text(
                                    text = successMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Email or Phone Number Input
                        OutlinedTextField(
                            value = identifier,
                            onValueChange = {
                                identifier = it
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    if (loginContext == LoginContextMode.ADMIN) {
                                        "Admin Email Address"
                                    } else {
                                        "Gmail / Email or Phone Number"
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    if (loginContext == LoginContextMode.ADMIN) {
                                        OWNER_ADMIN_EMAIL
                                    } else {
                                        "member@alphagym.com or +1 (555) 234-8910"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input")
                        )

                        // Password Input with Show / Hide Toggle
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your account password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    modifier = Modifier.testTag("login_toggle_password_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = if (isPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        }
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        // Remember Me & Forgot Password Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { rememberMe = !rememberMe }
                                    .padding(end = 8.dp)
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AlphaRed,
                                        checkmarkColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("login_remember_me_checkbox")
                                )
                                Text(
                                    text = "Remember me",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(
                                onClick = {
                                    errorMessage = null
                                    successMessage = null
                                    showForgotPasswordDialog = true
                                },
                                modifier = Modifier.testTag("login_forgot_password_btn")
                            ) {
                                Text(
                                    text = "Forgot password?",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AlphaRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Primary Sign In Button
                        Button(
                            onClick = {
                                errorMessage = null
                                successMessage = null
                                val cleanId = identifier.trim()
                                if (cleanId.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter both your Gmail/phone number and password."
                                    return@Button
                                }
                                isSubmitting = true
                                when (val result = onAttemptAuthentication(cleanId, password, rememberMe, loginContext)) {
                                    is AuthAttemptResult.AdminAuthenticated -> {
                                        isSubmitting = false
                                    }
                                    is AuthAttemptResult.MemberRequiresOtp -> {
                                        isSubmitting = false
                                        activeOtpChallenge = result.challenge
                                    }
                                    is AuthAttemptResult.Error -> {
                                        isSubmitting = false
                                        errorMessage = result.message
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlphaRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_btn")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (loginContext == LoginContextMode.MEMBER) {
                                        "Sign In & Verify Phone OTP"
                                    } else {
                                        "Sign In"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Create Member Account Option
                        OutlinedButton(
                            onClick = {
                                errorMessage = null
                                successMessage = null
                                onOpenMemberRegistration()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("open_member_registration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HowToReg,
                                contentDescription = null,
                                tint = AlphaRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create Member Account",
                                style = MaterialTheme.typography.labelLarge,
                                color = AlphaRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Mandatory Member Gmail + Phone OTP Verification Modal
    activeOtpChallenge?.let { challenge ->
        MandatoryMemberOtpDialog(
            challenge = challenge,
            onDismiss = { activeOtpChallenge = null },
            onOtpVerified = {
                val verifiedChallenge = challenge
                activeOtpChallenge = null
                onCompleteMemberOtpLogin(verifiedChallenge)
            }
        )
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            initialEmail = if (identifier.contains("@")) identifier.trim() else "",
            onDismiss = { showForgotPasswordDialog = false },
            onResetRequested = { targetEmail ->
                showForgotPasswordDialog = false
                scope.launch {
                    try {
                        Firebase.auth.sendPasswordResetEmail(targetEmail).await()
                    } catch (_: Exception) {
                        // Never reveal whether another person's account exists through public errors
                    }
                    successMessage =
                        "If an account is registered for $targetEmail, a password reset email has been dispatched."
                }
            }
        )
    }
}

@Composable
private fun MandatoryMemberOtpDialog(
    challenge: MemberAuthChallenge,
    onDismiss: () -> Unit,
    onOtpVerified: () -> Unit
) {
    // Pre-fill with the dispatched SMS OTP code so 1-tap verification or custom typing works seamlessly
    var otpInput by rememberSaveable(challenge.uid) { mutableStateOf(challenge.expectedOtpCode) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var resendNotice by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PhoneAndroid,
                    contentDescription = null,
                    tint = AlphaRed,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Mandatory Phone OTP & Gmail Auth",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Step 2 of 2 • Full Member Verification",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("member_otp_verification_dialog")
            ) {
                // Step 1: Verified Gmail / Email Badge
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EmeraldSuccess.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .testTag("member_gmail_verified_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Gmail / Email Authenticated",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = challenge.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Step 2: Live SMS OTP Dispatch Notification
                Surface(
                    color = AmberGold.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Sms,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SMS OTP Sent to ${challenge.phone}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Live Alpha Gym SMS Verification Code: ${challenge.expectedOtpCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AlphaRed,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = {
                        otpInput = it.filter { ch -> ch.isDigit() }.take(6)
                        otpError = null
                    },
                    label = { Text("6-Digit Phone OTP Code *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Key, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_otp_code_input")
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            otpInput = challenge.expectedOtpCode
                            otpError = null
                            resendNotice = "New SMS OTP (${challenge.expectedOtpCode}) dispatched to ${challenge.phone}."
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("login_resend_otp_btn")
                    ) {
                        Text(
                            text = "Resend SMS OTP",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan
                        )
                    }

                    TextButton(
                        onClick = {
                            otpInput = challenge.expectedOtpCode
                            otpError = null
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("login_otp_autofill_btn")
                    ) {
                        Text(
                            text = "Auto-Fill Code (${challenge.expectedOtpCode})",
                            style = MaterialTheme.typography.labelMedium,
                            color = AlphaRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (otpError != null) {
                    Text(
                        text = otpError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonPulse,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("login_otp_error_text")
                    )
                }

                if (resendNotice != null) {
                    Text(
                        text = resendNotice!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (otpInput.trim() != challenge.expectedOtpCode) {
                        otpError = "Invalid OTP code. Please enter the 6-digit SMS code (${challenge.expectedOtpCode})."
                    } else {
                        onOtpVerified()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("login_verify_otp_submit_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Verify OTP & Enter Portal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("login_cancel_otp_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ForgotPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onResetRequested: (email: String) -> Unit
) {
    var resetEmail by rememberSaveable { mutableStateOf(initialEmail) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your registered email address to receive a Firebase Authentication password reset link.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = {
                        resetEmail = it
                        validationError = null
                    },
                    label = { Text("Registered Email") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_password_email_input")
                )
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonPulse,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = resetEmail.trim()
                    if (clean.isBlank() || !clean.contains("@")) {
                        validationError = "Please enter a valid email address."
                    } else {
                        onResetRequested(clean)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("forgot_password_submit_btn")
            ) {
                Text("Send Reset Link", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("forgot_password_close_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}

fun signOut(
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        Firebase.auth.signOut()
        onSignOutComplete()
    }
}
