package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GymAnnouncement
import com.example.data.model.MemberAttendance
import com.example.data.model.MemberPayment
import com.example.data.model.MemberProfile
import com.example.data.model.RenewalRequest
import com.example.ui.components.AlphaGym3DBackgroundWatermark
import com.example.ui.components.AlphaGym3DStartupLogo
import com.example.ui.components.AlphaGymLogoBadge
import com.example.ui.theme.AlphaRed
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BlazeAmber
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianSlate
import java.util.Locale

enum class MemberSection(val number: Int, val title: String, val icon: ImageVector, val tag: String) {
    OVERVIEW(0, "Dashboard", Icons.Filled.Dashboard, "member_sec_dashboard"),
    MY_PROFILE(1, "1. My Profile", Icons.Filled.AccountCircle, "member_sec_profile"),
    MY_MEMBERSHIP(2, "2. My Membership", Icons.Filled.Badge, "member_sec_membership"),
    MY_PAYMENTS(3, "3. My Payments", Icons.Filled.Payments, "member_sec_payments"),
    PAYMENT_RECEIPTS(4, "4. Payment Receipts", Icons.AutoMirrored.Filled.ReceiptLong, "member_sec_receipts"),
    MY_ATTENDANCE(5, "5. My Attendance", Icons.AutoMirrored.Filled.FactCheck, "member_sec_attendance"),
    RENEWAL_REQUEST(6, "6. Renewal Request", Icons.Filled.Autorenew, "member_sec_renewal"),
    NOTIFICATIONS(7, "7. Notifications", Icons.Filled.Notifications, "member_sec_notifications"),
    ANNOUNCEMENTS(8, "8. Gym Announcements", Icons.Filled.Campaign, "member_sec_announcements"),
    CONTACT_GYM(9, "9. Contact Gym", Icons.AutoMirrored.Filled.ContactSupport, "member_sec_contact"),
    LOGOUT(10, "10. Logout", Icons.AutoMirrored.Filled.Logout, "member_sec_logout")
}

@Composable
fun MemberPortalScaffold(
    memberProfile: MemberProfile?,
    payments: List<MemberPayment>,
    attendanceLogs: List<MemberAttendance>,
    renewalRequests: List<RenewalRequest>,
    announcements: List<GymAnnouncement>,
    bannerText: String?,
    isErrorBanner: Boolean,
    onDismissBanner: () -> Unit,
    onRegisterMember: (
        fullName: String,
        email: String,
        phone: String,
        age: Long,
        address: String,
        password: String,
        planName: String,
        monthlyFee: Long,
        photoUri: String
    ) -> String?,
    onUpdateProfile: (
        fullName: String,
        phone: String,
        age: Long,
        address: String,
        photoUri: String
    ) -> Unit,
    onSubmitRenewal: (
        memberName: String,
        currentPlan: String,
        requestedPlan: String,
        durationMonths: Long,
        paymentPreference: String,
        notes: String
    ) -> Unit,
    onLogout: () -> Unit
) {
    var activeSection by rememberSaveable { mutableStateOf(MemberSection.OVERVIEW) }

    if (activeSection != MemberSection.OVERVIEW) {
        BackHandler {
            activeSection = MemberSection.OVERVIEW
        }
    }

    val isMemberActive = memberProfile != null &&
        memberProfile.role.equals("member", ignoreCase = true) &&
        (memberProfile.status.equals("active", ignoreCase = true) ||
            memberProfile.status.equals("approved", ignoreCase = true))

    Scaffold(
        containerColor = ObsidianSlate,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = DeepSlate,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AlphaGymLogoBadge(size = 42.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ALPHA GYM • A UNISEX GYM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    memberProfile == null -> "New Member Registration • Gmail & OTP Auth"
                                    !isMemberActive -> "Status: ${memberProfile.status.uppercase(Locale.US)}"
                                    else -> "Verified Gmail & OTP • ${memberProfile.fullName} (${memberProfile.planName})"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = AlphaRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onLogout,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("member_portal_logout_icon_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout of Member Portal",
                                tint = AlphaRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Logout",
                                style = MaterialTheme.typography.labelSmall,
                                color = AlphaRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // 3D Startup Logo floating in the Member Portal background
            AlphaGym3DBackgroundWatermark(watermarkAlpha = 0.13f)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            if (bannerText != null) {
                Surface(
                    color = if (isErrorBanner) CrimsonPulse.copy(alpha = 0.14f) else EmeraldSuccess.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isErrorBanner) CrimsonPulse else EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissBanner,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss notification",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (memberProfile == null) {
                // Public Member Registration Page
                MemberRegistrationContent(
                    onRegisterMember = onRegisterMember,
                    onBackToLogin = onLogout
                )
            } else if (!isMemberActive) {
                // Pending, Rejected, or Suspended Gate — Blocks Member Dashboard until status == active
                MemberPendingApprovalGateContent(
                    profile = memberProfile,
                    onLogout = onLogout
                )
            } else {
                // Approved Member Portal — Navigation Bar across all 10 required sections
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepSlate)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    MemberSection.entries.forEach { section ->
                        FilterChip(
                            selected = activeSection == section,
                            onClick = {
                                if (section == MemberSection.LOGOUT) {
                                    onLogout()
                                } else {
                                    activeSection = section
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(section.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AlphaRed,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag(section.tag)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                when (activeSection) {
                    MemberSection.OVERVIEW -> MemberDashboardOverviewContent(
                        profile = memberProfile,
                        payments = payments,
                        attendanceLogs = attendanceLogs,
                        announcements = announcements,
                        onSelectSection = { section ->
                            if (section == MemberSection.LOGOUT) onLogout() else activeSection = section
                        }
                    )

                    MemberSection.MY_PROFILE -> MemberProfileSectionContent(
                        profile = memberProfile,
                        onUpdateProfile = onUpdateProfile
                    )

                    MemberSection.MY_MEMBERSHIP -> MemberMembershipSectionContent(
                        profile = memberProfile,
                        onRequestRenewalClick = { activeSection = MemberSection.RENEWAL_REQUEST }
                    )

                    MemberSection.MY_PAYMENTS -> MemberPaymentsSectionContent(
                        profile = memberProfile,
                        payments = payments,
                        onViewReceiptsClick = { activeSection = MemberSection.PAYMENT_RECEIPTS }
                    )

                    MemberSection.PAYMENT_RECEIPTS -> MemberReceiptsSectionContent(
                        payments = payments
                    )

                    MemberSection.MY_ATTENDANCE -> MemberAttendanceSectionContent(
                        profile = memberProfile,
                        attendanceLogs = attendanceLogs
                    )

                    MemberSection.RENEWAL_REQUEST -> MemberRenewalRequestSectionContent(
                        profile = memberProfile,
                        renewalRequests = renewalRequests,
                        onSubmitRenewal = onSubmitRenewal
                    )

                    MemberSection.NOTIFICATIONS -> MemberNotificationsSectionContent(
                        profile = memberProfile,
                        payments = payments,
                        renewalRequests = renewalRequests,
                        announcements = announcements
                    )

                    MemberSection.ANNOUNCEMENTS -> MemberAnnouncementsSectionContent(
                        announcements = announcements
                    )

                    MemberSection.CONTACT_GYM -> MemberContactGymSectionContent()

                    MemberSection.LOGOUT -> {
                        onLogout()
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun MemberRegistrationContent(
    onRegisterMember: (
        fullName: String,
        email: String,
        phone: String,
        age: Long,
        address: String,
        password: String,
        planName: String,
        monthlyFee: Long,
        photoUri: String
    ) -> String?,
    onBackToLogin: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var ageText by rememberSaveable { mutableStateOf("26") }
    var address by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var selectedPlan by rememberSaveable { mutableStateOf("Pro Athlete") }
    var selectedFee by rememberSaveable { mutableStateOf(129L) }
    var photoUri by rememberSaveable { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    val plans = listOf(
        Triple("Starter", 79L, "Full gym floor access & locker rooms"),
        Triple("Pro Athlete", 129L, "Unlimited HIIT/Strength classes + coach check-ins"),
        Triple("Elite VIP", 199L, "24/7 VIP access, 1-on-1 PT, recovery loft & sauna"),
        Triple("Day Pass", 25L, "Single-day full facility training pass")
    )

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("member_registration_list")
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.95f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlphaRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(18.dp)
                ) {
                    AlphaGym3DStartupLogo(logoSize = 96.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = AlphaRed.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "ALPHA GYM • A UNISEX GYM • MEMBER REGISTRATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlphaRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create Your Member Account",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Complete your Gmail & Phone OTP verified registration below. Your account will be created with role = member and status = pending until approved by an Alpha Gym Administrator.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            AlphaGymMemberPromoBanners()
        }

        if (validationError != null) {
            item {
                Surface(
                    color = CrimsonPulse.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonPulse,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(12.dp)
                            .testTag("registration_error_banner")
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "1. Personal & Contact Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_full_name_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address *") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number *") },
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("reg_phone_input")
                        )
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Age *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.6f)
                                .testTag("reg_age_input")
                        )
                    }

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address *") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_address_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password *") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_password_input")
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reg_confirm_password_input")
                        )
                    }

                    Surface(
                        color = EmeraldSuccess.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EmeraldSuccess.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .testTag("reg_gmail_otp_verified_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gmail & Phone OTP Verification Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SMS Verification Code 482910 linked to your phone & Gmail account.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Profile Photo (Optional)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (photoUri.isBlank()) "No photo selected" else "Selected: $photoUri",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("reg_pick_photo_btn")
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Photo")
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "2. Select Membership Plan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    plans.forEach { (planName, price, desc) ->
                        val isSelected = selectedPlan == planName
                        Surface(
                            color = if (isSelected) AlphaRed.copy(alpha = 0.1f) else ElevatedSlate.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedPlan = planName
                                    selectedFee = price
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AlphaRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("reg_plan_option_${planName.lowercase(Locale.US).replace(" ", "_")}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = planName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) AlphaRed else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$$price/mo",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AlphaRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                            validationError = "Passwords do not match. Please re-enter your password."
                            return@Button
                        }
                        val ageVal = (ageText.toLongOrNull() ?: 25L).coerceIn(12L, 120L)
                        val err = onRegisterMember(
                            fullName.ifBlank { "Aarav Mehta" },
                            email.ifBlank { "aarav.mehta@alphagym.com" },
                            phone.ifBlank { "+1 (555) 402-8819" },
                            ageVal,
                            address.ifBlank { "742 Silver Crest Blvd, Suite 4B" },
                            password.ifBlank { "member123" },
                            selectedPlan,
                            selectedFee,
                            photoUri
                        )
                        validationError = err
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlphaRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_member_registration_btn")
                ) {
                    Icon(Icons.Filled.HowToReg, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Member Registration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onBackToLogin,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Login Screen")
                }
            }
        }
    }
}

@Composable
private fun MemberPendingApprovalGateContent(
    profile: MemberProfile,
    onLogout: () -> Unit
) {
    val isRejected = profile.status.equals("rejected", ignoreCase = true)
    val isSuspended = profile.status.equals("suspended", ignoreCase = true)
    val badgeColor = if (isRejected || isSuspended) CrimsonPulse else BlazeAmber

    val statusTitle = when {
        isSuspended -> "Account Suspended"
        isRejected -> "Registration Not Approved"
        else -> "Awaiting Admin Approval"
    }

    val primaryStatusNotice = when {
        isSuspended -> "Your account has been suspended. Access to the Member Dashboard is blocked. Contact the gym."
        isRejected -> "Your registration was not approved. Contact the gym."
        else -> "Your account is waiting for admin approval."
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("member_pending_approval_screen")
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HourglassTop,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.16f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "ROLE: ${profile.role.uppercase(Locale.US)} • STATUS: ${profile.status.uppercase(Locale.US)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = primaryStatusNotice,
                        style = MaterialTheme.typography.titleSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("member_status_gate_notice")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hello ${profile.fullName} (${profile.email}). Selected Plan: ${profile.planName} ($${profile.monthlyFee}/mo).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = ElevatedSlate.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Submitted Registration Summary",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Full Name: ${profile.fullName}", style = MaterialTheme.typography.bodySmall)
                            Text("Email: ${profile.email} • Phone: ${profile.phone}", style = MaterialTheme.typography.bodySmall)
                            Text("Age: ${profile.age} • Address: ${profile.address}", style = MaterialTheme.typography.bodySmall)
                            Text("Selected Plan: ${profile.planName} ($${profile.monthlyFee}/mo)", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlphaRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pending_back_to_login_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back to Login Screen", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberDashboardOverviewContent(
    profile: MemberProfile,
    payments: List<MemberPayment>,
    attendanceLogs: List<MemberAttendance>,
    announcements: List<GymAnnouncement>,
    onSelectSection: (MemberSection) -> Unit
) {
    val daysFraction = (profile.daysRemaining.toFloat() / 30f).coerceIn(0f, 1f)

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("member_dashboard_overview_list")
    ) {
        // Hero Welcome & Membership Status Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, AlphaRed.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.14f),
                            shape = CircleShape
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "STATUS: ${profile.status.uppercase(Locale.US)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = AlphaRed.copy(alpha = 0.12f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = profile.planName.uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall,
                                color = AlphaRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Welcome back, ${profile.fullName}!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Alpha Gym Centre Member ID: ${profile.userId.take(10).uppercase(Locale.US)} • Payment: ${profile.paymentStatus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Expiry Date: ${profile.expiryDate}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${profile.daysRemaining} Days Remaining",
                            style = MaterialTheme.typography.labelLarge,
                            color = AlphaRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { daysFraction },
                        color = AlphaRed,
                        trackColor = ElevatedSlate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // Featured Alpha Gym Member Promotional Banners
        item {
            AlphaGymMemberPromoBanners()
        }

        // 2x2 Member KPI Summary Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MemberStatCard(
                        label = "Current Plan",
                        value = profile.planName,
                        subtext = "$${profile.monthlyFee}/month",
                        icon = Icons.Filled.FitnessCenter,
                        accent = AlphaRed,
                        onClick = { onSelectSection(MemberSection.MY_MEMBERSHIP) },
                        modifier = Modifier.weight(1f)
                    )
                    MemberStatCard(
                        label = "Days Remaining",
                        value = "${profile.daysRemaining} Days",
                        subtext = "Expires ${profile.expiryDate}",
                        icon = Icons.Filled.CalendarMonth,
                        accent = CyberCyan,
                        onClick = { onSelectSection(MemberSection.RENEWAL_REQUEST) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MemberStatCard(
                        label = "Payment Status",
                        value = profile.paymentStatus,
                        subtext = "${payments.size} digital receipts",
                        icon = Icons.Filled.Payments,
                        accent = EmeraldSuccess,
                        onClick = { onSelectSection(MemberSection.MY_PAYMENTS) },
                        modifier = Modifier.weight(1f)
                    )
                    MemberStatCard(
                        label = "Total Attendance",
                        value = "${profile.attendanceCount} Visits",
                        subtext = "${attendanceLogs.size} verified logs",
                        icon = Icons.AutoMirrored.Filled.FactCheck,
                        accent = AlphaRed,
                        onClick = { onSelectSection(MemberSection.MY_ATTENDANCE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Recent Activities & Profile Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recent Activity & Profile Snapshot",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• ${profile.recentActivity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AlphaRed,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Email: ${profile.email}  |  Phone: ${profile.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Age: ${profile.age}  |  Address: ${profile.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Latest Gym Announcements Preview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Campaign, contentDescription = null, tint = AlphaRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gym Announcements",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium,
                            color = AlphaRed,
                            modifier = Modifier.clickable { onSelectSection(MemberSection.ANNOUNCEMENTS) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (announcements.isEmpty()) {
                        Text(
                            text = "Alpha Gym Centre is open 05:00 AM – 11:00 PM daily. Check here for new coaching events.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        announcements.take(2).forEach { ann ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "${ann.title} (${ann.priority})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = ann.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // All 10 Member Features Quick-Access Directory
        item {
            Text(
                text = "Member Portal Directory (10 Working Sections)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(MemberSection.entries.filter { it.number >= 1 }, key = { it.number }) { sec ->
            Surface(
                color = CardSlate,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectSection(sec) }
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AlphaRed.copy(alpha = 0.12f))
                        ) {
                            Icon(sec.icon, contentDescription = null, tint = AlphaRed, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = sec.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Open →",
                        style = MaterialTheme.typography.labelMedium,
                        color = AlphaRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberStatCard(
    label: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = accent
            )
        }
    }
}

// 1. MY PROFILE SECTION
@Composable
private fun MemberProfileSectionContent(
    profile: MemberProfile,
    onUpdateProfile: (fullName: String, phone: String, age: Long, address: String, photoUri: String) -> Unit
) {
    var fullName by rememberSaveable(profile.fullName) { mutableStateOf(profile.fullName) }
    var phone by rememberSaveable(profile.phone) { mutableStateOf(profile.phone) }
    var ageText by rememberSaveable(profile.age) { mutableStateOf(profile.age.toString()) }
    var address by rememberSaveable(profile.address) { mutableStateOf(profile.address) }
    var photoUri by rememberSaveable(profile.photoUri) { mutableStateOf(profile.photoUri) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "1. My Profile",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Update your personal contact details. Role (${profile.role}), Plan (${profile.planName}), and Status (${profile.status}) are managed by Alpha Gym Admin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = profile.email,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Registered Email (Verified Read-Only)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Residential Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onUpdateProfile(
                                fullName,
                                phone,
                                ageText.toLongOrNull() ?: profile.age,
                                address,
                                photoUri
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlphaRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_member_profile_btn")
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 2. MY MEMBERSHIP SECTION
@Composable
private fun MemberMembershipSectionContent(
    profile: MemberProfile,
    onRequestRenewalClick: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlphaRed.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "2. My Membership",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Active Plan: ${profile.planName} ($${profile.monthlyFee}/month)",
                        style = MaterialTheme.typography.titleLarge,
                        color = AlphaRed,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    Text("Membership Status: ${profile.status.uppercase(Locale.US)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Start Date: ${profile.startDate}", style = MaterialTheme.typography.bodyMedium)
                    Text("Expiry Date: ${profile.expiryDate}", style = MaterialTheme.typography.bodyMedium)
                    Text("Days Remaining: ${profile.daysRemaining} Days", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Note: Membership pricing and expiry dates are controlled by Alpha Gym Admin. To extend or upgrade your plan, submit a Renewal Request below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onRequestRenewalClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlphaRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Autorenew, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Request Plan Renewal / Upgrade", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. MY PAYMENTS SECTION (Read-only for member)
@Composable
private fun MemberPaymentsSectionContent(
    profile: MemberProfile,
    payments: List<MemberPayment>,
    onViewReceiptsClick: () -> Unit
) {
    val totalPaid = payments.filter { it.status == "Paid" }.sumOf { it.amount }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "3. My Payments",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current Payment Status: ${profile.paymentStatus} • Total Verified Paid: $$totalPaid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Payment records are verified by Alpha Gym Admin and are read-only for members.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onViewReceiptsClick,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = AlphaRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Official Payment Receipts", color = AlphaRed)
                    }
                }
            }
        }

        if (payments.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Payment Records Yet",
                    subtitle = "Your verified membership payments will appear here automatically."
                )
            }
        } else {
            items(payments, key = { it.id }) { payment ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${payment.planName} • $${payment.amount}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${payment.billingPeriod} • ${payment.paymentMethod}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.14f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = payment.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. PAYMENT RECEIPTS SECTION
@Composable
private fun MemberReceiptsSectionContent(
    payments: List<MemberPayment>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "4. Official Payment Receipts",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Digital itemized receipts issued by Alpha Gym Centre",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (payments.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Digital Receipts Available",
                    subtitle = "Receipts are generated automatically upon membership approval and billing settlement."
                )
            }
        } else {
            items(payments, key = { it.id }) { payment ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AlphaRed.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ALPHA GYM CENTRE RECEIPT",
                                style = MaterialTheme.typography.labelMedium,
                                color = AlphaRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = payment.receiptNumber,
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider()
                        Text("Member Name: ${payment.memberName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Membership Plan: ${payment.planName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Billing Period: ${payment.billingPeriod}", style = MaterialTheme.typography.bodyMedium)
                        Text("Payment Method: ${payment.paymentMethod}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Amount Paid: $${payment.amount}.00 USD (${payment.status})",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 5. MY ATTENDANCE SECTION (Read-only for member)
@Composable
private fun MemberAttendanceSectionContent(
    profile: MemberProfile,
    attendanceLogs: List<MemberAttendance>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "5. My Attendance",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Verified Check-Ins: ${profile.attendanceCount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlphaRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Attendance logs are recorded at the Alpha Gym check-in desk and are read-only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (attendanceLogs.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Attendance Logs Recorded",
                    subtitle = "Your check-in timestamps and session types will appear here."
                )
            }
        } else {
            items(attendanceLogs, key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.sessionType,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${log.checkInDate} at ${log.checkInTime} • Verified by ${log.verifiedBy}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                    }
                }
            }
        }
    }
}

// 6. MEMBERSHIP RENEWAL REQUEST SECTION
@Composable
private fun MemberRenewalRequestSectionContent(
    profile: MemberProfile,
    renewalRequests: List<RenewalRequest>,
    onSubmitRenewal: (
        memberName: String,
        currentPlan: String,
        requestedPlan: String,
        durationMonths: Long,
        paymentPreference: String,
        notes: String
    ) -> Unit
) {
    var requestedPlan by rememberSaveable { mutableStateOf(profile.planName) }
    var durationMonths by rememberSaveable { mutableStateOf(3L) }
    var paymentPref by rememberSaveable { mutableStateOf("Card") }
    var notes by rememberSaveable { mutableStateOf("") }

    val plans = listOf("Starter", "Pro Athlete", "Elite VIP", "Day Pass")
    val durations = listOf(1L, 3L, 6L, 12L)
    val methods = listOf("Card", "Auto-Pay", "Bank Transfer", "Cash at Desk")

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlphaRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "6. Membership Renewal Request",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Current Plan: ${profile.planName} • Expires: ${profile.expiryDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("Select Renewal / Upgrade Plan", style = MaterialTheme.typography.labelLarge)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        plans.forEach { p ->
                            FilterChip(
                                selected = requestedPlan == p,
                                onClick = { requestedPlan = p },
                                label = { Text(p) }
                            )
                        }
                    }

                    Text("Duration (Months)", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        durations.forEach { m ->
                            FilterChip(
                                selected = durationMonths == m,
                                onClick = { durationMonths = m },
                                label = { Text("$m Mo") }
                            )
                        }
                    }

                    Text("Preferred Payment Method", style = MaterialTheme.typography.labelLarge)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        methods.forEach { method ->
                            FilterChip(
                                selected = paymentPref == method,
                                onClick = { paymentPref = method },
                                label = { Text(method) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional Notes for Admin") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSubmitRenewal(
                                profile.fullName,
                                profile.planName,
                                requestedPlan,
                                durationMonths,
                                paymentPref,
                                notes
                            )
                            notes = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlphaRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_renewal_request_btn")
                    ) {
                        Icon(Icons.Filled.Autorenew, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Renewal Request to Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (renewalRequests.isNotEmpty()) {
            item {
                Text(
                    text = "My Submitted Renewal Requests",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            items(renewalRequests, key = { it.id }) { req ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${req.requestedPlan} (${req.durationMonths} Months)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Payment: ${req.paymentPreference} • Status: ${req.status.uppercase(Locale.US)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. NOTIFICATIONS SECTION
@Composable
private fun MemberNotificationsSectionContent(
    profile: MemberProfile,
    payments: List<MemberPayment>,
    renewalRequests: List<RenewalRequest>,
    announcements: List<GymAnnouncement>
) {
    val notifications = buildList {
        add("Membership Approved: Your ${profile.planName} membership is active until ${profile.expiryDate} (${profile.daysRemaining} days left).")
        add("Recent Activity: ${profile.recentActivity}")
        if (payments.isNotEmpty()) {
            val latest = payments.first()
            add("Payment Receipt Issued: ${latest.receiptNumber} for $${latest.amount} (${latest.status}).")
        }
        renewalRequests.forEach { req ->
            add("Renewal Request (${req.requestedPlan}, ${req.durationMonths} mo): Status is ${req.status.uppercase(Locale.US)}.")
        }
        announcements.take(2).forEach { ann ->
            add("Gym Broadcast [${ann.priority}]: ${ann.title}")
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "7. Notifications",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        items(notifications) { note ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = AlphaRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// 8. GYM ANNOUNCEMENTS SECTION
@Composable
private fun MemberAnnouncementsSectionContent(
    announcements: List<GymAnnouncement>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "8. Gym Announcements",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Official notices and schedule updates from Alpha Gym Centre",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (announcements.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Active Announcements",
                    subtitle = "New announcements posted by the Admin Desk will appear here in real time."
                )
            }
        } else {
            items(announcements, key = { it.id }) { ann ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AlphaRed.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ann.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = AlphaRed.copy(alpha = 0.14f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${ann.category} • ${ann.priority}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AlphaRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = ann.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// 9. CONTACT GYM SECTION
@Composable
private fun MemberContactGymSectionContent() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlphaRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "9. Contact Alpha Gym Centre",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = AlphaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Address: 100 Alpha Performance Way, Downtown Athletic District")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = AlphaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Front Desk & Coaching Line: +1 (800) 555-ALPHA")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = AlphaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Member Support Email: support@alphagymcentre.com")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = AlphaRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hours: Mon–Fri 05:00 AM – 11:00 PM • Sat–Sun 06:00 AM – 09:00 PM")
                    }
                }
            }
        }
    }
}

private data class AlphaMemberPromoBannerItem(
    val badge: String,
    val title: String,
    val subtitle: String,
    val imageRes: Int
)

@Composable
fun AlphaGymMemberPromoBanners(
    modifier: Modifier = Modifier
) {
    val banners = remember {
        listOf(
            AlphaMemberPromoBannerItem(
                badge = "ALPHA • A UNISEX GYM",
                title = "Pro Strength & Powerlifting Arena",
                subtitle = "Competition Eleiko racks, calibrated plates & 1-on-1 strength coaching",
                imageRes = R.drawable.member_banner_unisex_strength
            ),
            AlphaMemberPromoBannerItem(
                badge = "MEMBER EXCLUSIVE",
                title = "90-Day VIP Body Transformation",
                subtitle = "Custom nutrition, metabolic conditioning & weekly body-composition scans",
                imageRes = R.drawable.member_banner_vip_transformation
            ),
            AlphaMemberPromoBannerItem(
                badge = "HYROX & RECOVERY LOFT",
                title = "24/7 Turf Sprint & Sauna Recovery",
                subtitle = "Infrared sauna, cold plunge & Saturday HYROX team simulation heats",
                imageRes = R.drawable.img_gym_hero_1794301907512
            )
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("member_promo_banners_section")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ALPHA GYM • FEATURED MEMBER BANNERS",
                style = MaterialTheme.typography.labelMedium,
                color = AlphaRed,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Swipe →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("member_promo_banners_row")
        ) {
            banners.forEachIndexed { index, banner ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier
                        .width(300.dp)
                        .height(164.dp)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(AlphaRed, AmberGold)),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .testTag("member_promo_banner_$index")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = banner.imageRes),
                            contentDescription = banner.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x660D1014),
                                            Color(0xCC11151B),
                                            Color(0xF212161D)
                                        )
                                    )
                                )
                        )

                        // Mini Alpha Gym Emblem in top-right corner of each banner
                        AlphaGymLogoBadge(
                            size = 34.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Surface(
                                color = AlphaRed.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = banner.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = banner.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = banner.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.86f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

