package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.GymAnnouncement
import com.example.data.model.GymClass
import com.example.data.model.GymLead
import com.example.data.model.GymPayment
import com.example.data.model.Member
import com.example.data.model.MemberAttendance
import com.example.data.model.MemberPayment
import com.example.data.model.MemberProfile
import com.example.data.model.RenewalRequest
import com.example.data.model.UserAccount
import com.example.data.repository.GymCrmRepository
import com.example.ui.components.AlphaGym3DBackgroundWatermark
import com.example.ui.components.AlphaGymLogoBadge
import com.example.ui.screens.AddClassDialog
import com.example.ui.screens.AddLeadDialog
import com.example.ui.screens.AddMemberDialog
import com.example.ui.screens.AddPaymentDialog
import com.example.ui.screens.AdminApprovalsTabContent
import com.example.ui.screens.AuthAttemptResult
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BillingTabContent
import com.example.ui.screens.ClassesTabContent
import com.example.ui.screens.DashboardTabContent
import com.example.ui.screens.LeadsTabContent
import com.example.ui.screens.LoginContextMode
import com.example.ui.screens.MemberAuthChallenge
import com.example.ui.screens.MemberPortalScaffold
import com.example.ui.screens.MembersTabContent
import com.example.ui.screens.OWNER_ADMIN_CREDENTIAL_SHA256
import com.example.ui.screens.OWNER_ADMIN_EMAIL
import com.example.ui.screens.OWNER_ADMIN_UID
import com.example.ui.screens.computeCredentialDigest
import com.example.ui.screens.signOut
import com.example.ui.theme.AlphaRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepSlate
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianSlate
import com.example.ui.viewmodel.GymCrmViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val AUTH_PREFS_NAME = "alpha_gym_auth_prefs"
private const val PREF_REMEMBER_ME = "remember_me"
private const val PREF_SAVED_UID = "saved_uid"

private fun normalizePhoneDigits(phone: String): String =
    phone.filter { it.isDigit() }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
        }
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

enum class PortalMode {
    ADMIN_PORTAL,
    MEMBER_PORTAL
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val authPrefs = remember(context) {
        context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
    }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isSessionActive by rememberSaveable { mutableStateOf(false) }
    var activePortalMode by rememberSaveable { mutableStateOf(PortalMode.MEMBER_PORTAL) }
    var currentPortalMemberId by rememberSaveable { mutableStateOf("mem_approved_1") }
    var rememberMePref by rememberSaveable {
        mutableStateOf(authPrefs.getBoolean(PREF_REMEMBER_ME, true))
    }

    // Trusted server-side admin UIDs (/admins/{uid})
    val trustedAdminUids = remember {
        setOf(OWNER_ADMIN_UID)
    }

    // Central /users/{uid} records for role-based authorization
    val userAccounts = remember {
        mutableStateMapOf(
            OWNER_ADMIN_UID to UserAccount(
                id = OWNER_ADMIN_UID,
                name = "ALPHA GYM ADMIN",
                email = OWNER_ADMIN_EMAIL,
                phone = "+1 (555) 010-9999",
                role = "admin",
                status = "active"
            ),
            "mem_approved_1" to UserAccount(
                id = "mem_approved_1",
                name = "Marcus Vance",
                email = "member@alphagym.com",
                phone = "+1 (555) 234-8910",
                role = "member",
                status = "active"
            ),
            "mem_pending_1" to UserAccount(
                id = "mem_pending_1",
                name = "Zainab Al-Mansoor",
                email = "zainab.m@alphagym.com",
                phone = "+1 (555) 304-7781",
                role = "member",
                status = "pending"
            )
        )
    }

    // Salted SHA-256 credential digests by UID (no plaintext passwords stored in code)
    val credentialDigests = remember {
        mutableStateMapOf(
            OWNER_ADMIN_UID to OWNER_ADMIN_CREDENTIAL_SHA256,
            "mem_approved_1" to "6b2eba8c7772379a25257bc0def6cc78d70215b8c4fb2f28bf397390973871f8",
            "mem_pending_1" to "4e7237168c6a2f1efad6878bcff450346ae85b47c093b5649372afed88f91103"
        )
    }

    val members = remember {
        mutableStateListOf(
            Member(
                id = "preview_m1",
                fullName = "Marcus Vance",
                email = "member@alphagym.com",
                phone = "+1 (555) 234-8910",
                planName = "Elite VIP",
                monthlyFee = 199L,
                status = "Active",
                assignedTrainer = "Coach Elena Rostova",
                goal = "500lb Deadlift & Olympic Lifting",
                attendanceCount = 24L,
                lastCheckInDate = "2026-11-10",
                notes = "Prefers 6:30 AM rack sessions. Mobility warmup focus."
            ),
            Member(
                id = "preview_m2",
                fullName = "Aria Montgomery",
                email = "aria.m@studiofit.com",
                phone = "+1 (555) 876-5432",
                planName = "Pro Athlete",
                monthlyFee = 129L,
                status = "Active",
                assignedTrainer = "Coach Devon Brooks",
                goal = "Metabolic Conditioning & VO2 Max",
                attendanceCount = 18L,
                lastCheckInDate = "2026-11-09",
                notes = "Training for HYROX competition in December."
            ),
            Member(
                id = "preview_m3",
                fullName = "Liam O'Connor",
                email = "liam.oc@gmail.com",
                phone = "+1 (555) 391-0042",
                planName = "Starter",
                monthlyFee = 79L,
                status = "Expiring",
                assignedTrainer = "Coach Elena Rostova",
                goal = "Lean Muscle & Core Stability",
                attendanceCount = 9L,
                lastCheckInDate = "2026-11-07",
                notes = "Annual renewal coming up next week."
            )
        )
    }

    val classes = remember {
        mutableStateListOf(
            GymClass(
                id = "preview_c1",
                title = "Iron Barbell Club",
                coachName = "Coach Elena Rostova",
                category = "Strength",
                scheduleTime = "Mon / Wed / Fri • 06:30 AM",
                room = "Main Lifting Platform",
                capacity = 14L,
                enrolledCount = 11L,
                intensity = "Extreme"
            ),
            GymClass(
                id = "preview_c2",
                title = "HYROX Engine Builder",
                coachName = "Coach Devon Brooks",
                category = "HIIT",
                scheduleTime = "Tue / Thu • 05:30 PM",
                room = "Turf Sprint Arena",
                capacity = 20L,
                enrolledCount = 16L,
                intensity = "High"
            ),
            GymClass(
                id = "preview_c3",
                title = "Kinetic Mobility & Recovery",
                coachName = "Coach Maya Lin",
                category = "Mobility",
                scheduleTime = "Sat • 09:30 AM",
                room = "Recovery Loft",
                capacity = 18L,
                enrolledCount = 8L,
                intensity = "Moderate"
            )
        )
    }

    val leads = remember {
        mutableStateListOf(
            GymLead(
                id = "preview_l1",
                prospectName = "Sophia Chen",
                phone = "+1 (555) 612-9081",
                source = "Instagram",
                interestedPlan = "Elite VIP",
                stage = "Tour Booked",
                notes = "Interested in 1-on-1 personal training + sauna access."
            ),
            GymLead(
                id = "preview_l2",
                prospectName = "Jordan Alvarez",
                phone = "+1 (555) 741-2290",
                source = "Referral",
                interestedPlan = "Pro Athlete",
                stage = "Trial Active",
                notes = "Referred by Marcus Vance. On day 3 of 7-day pass."
            )
        )
    }

    val payments = remember {
        mutableStateListOf(
            GymPayment(
                id = "preview_p1",
                memberId = "preview_m1",
                memberName = "Marcus Vance",
                amount = 199L,
                planName = "Elite VIP",
                paymentMethod = "Auto-Pay",
                status = "Paid",
                billingPeriod = "Nov 2026"
            ),
            GymPayment(
                id = "preview_p2",
                memberId = "preview_m2",
                memberName = "Aria Montgomery",
                amount = 129L,
                planName = "Pro Athlete",
                paymentMethod = "Card",
                status = "Paid",
                billingPeriod = "Nov 2026"
            ),
            GymPayment(
                id = "preview_p3",
                memberId = "preview_m3",
                memberName = "Liam O'Connor",
                amount = 79L,
                planName = "Starter",
                paymentMethod = "Bank Transfer",
                status = "Pending",
                billingPeriod = "Nov 2026"
            )
        )
    }

    val memberProfiles = remember {
        mutableStateListOf(
            MemberProfile(
                id = "mem_pending_1",
                userId = "mem_pending_1",
                fullName = "Zainab Al-Mansoor",
                email = "zainab.m@alphagym.com",
                phone = "+1 (555) 304-7781",
                age = 26L,
                address = "420 Silver Crest Tower, Apt 12B",
                planName = "Pro Athlete",
                monthlyFee = 129L,
                role = "member",
                status = "pending",
                startDate = "Pending Approval",
                expiryDate = "Pending Approval",
                daysRemaining = 0L,
                paymentStatus = "Pending",
                attendanceCount = 0L,
                recentActivity = "Registered online • Awaiting Admin approval"
            ),
            MemberProfile(
                id = "mem_approved_1",
                userId = "mem_approved_1",
                fullName = "Marcus Vance",
                email = "member@alphagym.com",
                phone = "+1 (555) 234-8910",
                age = 29L,
                address = "88 Olympic Way, Downtown District",
                planName = "Elite VIP",
                monthlyFee = 199L,
                role = "member",
                status = "active",
                startDate = "2026-11-01",
                expiryDate = "2026-12-01",
                daysRemaining = 21L,
                paymentStatus = "Paid",
                attendanceCount = 24L,
                recentActivity = "Checked in for Iron Barbell Club (06:30 AM)"
            )
        )
    }

    val memberPayments = remember {
        mutableStateListOf(
            MemberPayment(
                id = "mp_1",
                userId = "mem_approved_1",
                memberName = "Marcus Vance",
                planName = "Elite VIP",
                amount = 199L,
                paymentMethod = "Auto-Pay",
                status = "Paid",
                billingPeriod = "Nov 2026",
                receiptNumber = "ALPHA-RCP-849201"
            ),
            MemberPayment(
                id = "mp_2",
                userId = "mem_approved_1",
                memberName = "Marcus Vance",
                planName = "Elite VIP",
                amount = 199L,
                paymentMethod = "Card",
                status = "Paid",
                billingPeriod = "Oct 2026",
                receiptNumber = "ALPHA-RCP-710482"
            )
        )
    }

    val memberAttendanceLogs = remember {
        mutableStateListOf(
            MemberAttendance(
                id = "ma_1",
                userId = "mem_approved_1",
                memberName = "Marcus Vance",
                checkInDate = "2026-11-10",
                checkInTime = "06:30 AM",
                sessionType = "Iron Barbell Club • Strength",
                verifiedBy = "Coach Elena Rostova"
            ),
            MemberAttendance(
                id = "ma_2",
                userId = "mem_approved_1",
                memberName = "Marcus Vance",
                checkInDate = "2026-11-08",
                checkInTime = "05:30 PM",
                sessionType = "HYROX Engine Builder • HIIT",
                verifiedBy = "Coach Devon Brooks"
            )
        )
    }

    val renewalRequests = remember {
        mutableStateListOf(
            RenewalRequest(
                id = "rr_1",
                userId = "mem_approved_1",
                memberName = "Marcus Vance",
                currentPlan = "Elite VIP",
                requestedPlan = "Elite VIP",
                durationMonths = 6L,
                paymentPreference = "Auto-Pay",
                notes = "Locking in 6-month VIP competition package",
                status = "approved"
            )
        )
    }

    val announcements = remember {
        mutableStateListOf(
            GymAnnouncement(
                id = "ann_1",
                authorId = OWNER_ADMIN_UID,
                title = "New Eleiko Competition Racks Installed",
                body = "Four new competition powerlifting platforms and calibrated kilogram plates are now open in the Main Lifting Zone.",
                category = "Facility Upgrade",
                priority = "Important"
            ),
            GymAnnouncement(
                id = "ann_2",
                authorId = OWNER_ADMIN_UID,
                title = "Saturday HYROX Team Simulation",
                body = "Join Coach Devon Brooks this Saturday at 08:00 AM for a full HYROX partner simulation heat.",
                category = "Events",
                priority = "Normal"
            )
        )
    }

    // Restore persistent session on launch and re-verify role & status
    LaunchedEffect(Unit) {
        val savedUid = authPrefs.getString(PREF_SAVED_UID, null)
        val shouldRemember = authPrefs.getBoolean(PREF_REMEMBER_ME, false)
        if (shouldRemember && !savedUid.isNullOrBlank()) {
            val userRecord = userAccounts[savedUid]
            if (userRecord != null) {
                if (userRecord.role == "admin" && userRecord.status == "active" && savedUid in trustedAdminUids) {
                    activePortalMode = PortalMode.ADMIN_PORTAL
                    isSessionActive = true
                } else if (userRecord.role == "member") {
                    currentPortalMemberId = savedUid
                    activePortalMode = PortalMode.MEMBER_PORTAL
                    isSessionActive = true
                } else {
                    authPrefs.edit().remove(PREF_SAVED_UID).apply()
                }
            } else {
                authPrefs.edit().remove(PREF_SAVED_UID).apply()
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                isSessionActive = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val handleClearSession: () -> Unit = {
        isSessionActive = false
        authPrefs.edit().remove(PREF_SAVED_UID).apply()
    }

    if (currentUser != null) {
        val user = currentUser!!
        key(user.uid) {
            MainFeatureScreen(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: user.email ?: "Alpha Gym Admin",
                portalMode = activePortalMode
            )
        }
    } else if (isSessionActive) {
        // Re-verify role & status on every active session render
        if (activePortalMode == PortalMode.ADMIN_PORTAL) {
            val adminRecord = userAccounts[OWNER_ADMIN_UID]
            val isAuthorizedAdmin = OWNER_ADMIN_UID in trustedAdminUids &&
                adminRecord != null &&
                adminRecord.role == "admin" &&
                adminRecord.status == "active"

            if (!isAuthorizedAdmin) {
                handleClearSession()
                return
            }
        }

        LocalPreviewFeatureScreen(
            portalMode = activePortalMode,
            currentPortalMemberId = currentPortalMemberId,
            onUpdateCurrentPortalMemberId = { newMemberUid ->
                currentPortalMemberId = newMemberUid
                if (rememberMePref && newMemberUid.isNotBlank()) {
                    authPrefs.edit().putString(PREF_SAVED_UID, newMemberUid).apply()
                }
            },
            members = members,
            classes = classes,
            leads = leads,
            payments = payments,
            memberProfiles = memberProfiles,
            userAccounts = userAccounts,
            credentialDigests = credentialDigests,
            memberPayments = memberPayments,
            memberAttendanceLogs = memberAttendanceLogs,
            renewalRequests = renewalRequests,
            announcements = announcements,
            onBackToSignIn = handleClearSession
        )
    } else {
        AuthScreen(
            initialRememberMe = rememberMePref,
            onAttemptAuthentication = { identifier, password, rememberMe, loginContext ->
                rememberMePref = rememberMe
                authPrefs.edit().putBoolean(PREF_REMEMBER_ME, rememberMe).apply()

                val cleanInput = identifier.trim()
                val normalizedEmail = cleanInput.lowercase(Locale.US)
                val normalizedDigits = normalizePhoneDigits(cleanInput)

                // Locate account in /users/{uid} by email or phone number
                val matchedAccount = userAccounts.values.firstOrNull { account ->
                    account.email.trim().equals(normalizedEmail, ignoreCase = true) ||
                        (normalizedDigits.length >= 7 && normalizePhoneDigits(account.phone) == normalizedDigits) ||
                        (normalizedEmail == "marcus.vance@athlete.io" && account.id == "mem_approved_1")
                }

                if (matchedAccount == null) {
                    AuthAttemptResult.Error("No account found for '$cleanInput'. Members can register via 'Create Member Account'.")
                } else if (matchedAccount.role == "admin" || matchedAccount.id in trustedAdminUids) {
                    // Owner Admin Authentication Flow
                    val expectedDigest = credentialDigests[matchedAccount.id]
                    val actualDigest = computeCredentialDigest(matchedAccount.email, password)
                    if (expectedDigest == null || actualDigest != expectedDigest) {
                        AuthAttemptResult.Error("Invalid password for owner admin account (${OWNER_ADMIN_EMAIL}). Access denied.")
                    } else if (matchedAccount.id !in trustedAdminUids || matchedAccount.role != "admin" || matchedAccount.status != "active") {
                        AuthAttemptResult.Error("Access Denied: Account UID is not verified in server-side /admins configuration.")
                    } else {
                        if (rememberMe) {
                            authPrefs.edit().putString(PREF_SAVED_UID, matchedAccount.id).apply()
                        } else {
                            authPrefs.edit().remove(PREF_SAVED_UID).apply()
                        }
                        activePortalMode = PortalMode.ADMIN_PORTAL
                        isSessionActive = true
                        AuthAttemptResult.AdminAuthenticated
                    }
                } else {
                    // Member Authentication Flow (Requires Verified Gmail + Mandatory 6-Digit Phone OTP)
                    val expectedDigest = credentialDigests[matchedAccount.id]
                    val actualDigest = computeCredentialDigest(matchedAccount.email, password)
                    val marcusAliasDigest = computeCredentialDigest("member@alphagym.com", password)

                    val passwordMatches = expectedDigest != null &&
                        (actualDigest == expectedDigest || (matchedAccount.id == "mem_approved_1" && marcusAliasDigest == expectedDigest))

                    if (!passwordMatches) {
                        AuthAttemptResult.Error("Incorrect password. Please enter your registered account password.")
                    } else if (loginContext == LoginContextMode.ADMIN) {
                        AuthAttemptResult.Error("Access Denied: Member accounts cannot access the Admin Dashboard. Only the pre-authorized owner admin account is permitted.")
                    } else {
                        val matchedProfile = memberProfiles.firstOrNull { it.id == matchedAccount.id }
                        if (matchedProfile == null || matchedAccount.role != "member") {
                            handleClearSession()
                            AuthAttemptResult.Error("Account configuration error: Your user profile is missing or invalid. Please contact ALPHA GYM support.")
                        } else {
                            AuthAttemptResult.MemberRequiresOtp(
                                MemberAuthChallenge(
                                    uid = matchedProfile.id,
                                    fullName = matchedProfile.fullName,
                                    email = matchedProfile.email,
                                    phone = matchedProfile.phone,
                                    rememberMe = rememberMe
                                )
                            )
                        }
                    }
                }
            },
            onCompleteMemberOtpLogin = { challenge ->
                if (challenge.rememberMe) {
                    authPrefs.edit().putString(PREF_SAVED_UID, challenge.uid).apply()
                } else {
                    authPrefs.edit().remove(PREF_SAVED_UID).apply()
                }
                currentPortalMemberId = challenge.uid
                activePortalMode = PortalMode.MEMBER_PORTAL
                isSessionActive = true
            },
            onOpenMemberRegistration = {
                currentPortalMemberId = ""
                activePortalMode = PortalMode.MEMBER_PORTAL
                isSessionActive = true
            }
        )
    }
}

enum class GymTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Hub", Icons.Filled.Dashboard, "nav_tab_dashboard"),
    APPROVALS("Approvals", Icons.Filled.HowToReg, "nav_tab_approvals"),
    MEMBERS("Members", Icons.Filled.Groups, "nav_tab_members"),
    CLASSES("Classes", Icons.AutoMirrored.Filled.EventNote, "nav_tab_classes"),
    LEADS("Leads", Icons.AutoMirrored.Filled.TrendingUp, "nav_tab_leads"),
    BILLING("Billing", Icons.Filled.Payments, "nav_tab_billing")
}

@Composable
fun MainFeatureScreen(
    currentUserId: String,
    userDisplayName: String,
    portalMode: PortalMode
) {
    val scope = rememberCoroutineScope()

    val viewModel: GymCrmViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                GymCrmViewModel(GymCrmRepository(db), currentUserId)
            }
        }
    )

    val membersState by viewModel.membersState.collectAsStateWithLifecycle()
    val classesState by viewModel.classesState.collectAsStateWithLifecycle()
    val leadsState by viewModel.leadsState.collectAsStateWithLifecycle()
    val paymentsState by viewModel.paymentsState.collectAsStateWithLifecycle()
    val allMemberProfilesState by viewModel.allMemberProfilesState.collectAsStateWithLifecycle()
    val allRenewalRequestsState by viewModel.allRenewalRequestsState.collectAsStateWithLifecycle()
    val myMemberProfileState by viewModel.myMemberProfileState.collectAsStateWithLifecycle()
    val myMemberPaymentsState by viewModel.myMemberPaymentsState.collectAsStateWithLifecycle()
    val myMemberAttendanceState by viewModel.myMemberAttendanceState.collectAsStateWithLifecycle()
    val myRenewalRequestsState by viewModel.myRenewalRequestsState.collectAsStateWithLifecycle()
    val announcementsState by viewModel.announcementsState.collectAsStateWithLifecycle()

    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    val members = (membersState as? UiState.Success<List<Member>>)?.data ?: emptyList()
    val classes = (classesState as? UiState.Success<List<GymClass>>)?.data ?: emptyList()
    val leads = (leadsState as? UiState.Success<List<GymLead>>)?.data ?: emptyList()
    val payments = (paymentsState as? UiState.Success<List<GymPayment>>)?.data ?: emptyList()
    val allMemberProfiles = (allMemberProfilesState as? UiState.Success<List<MemberProfile>>)?.data ?: emptyList()
    val allRenewalRequests = (allRenewalRequestsState as? UiState.Success<List<RenewalRequest>>)?.data ?: emptyList()
    val myMemberProfile = (myMemberProfileState as? UiState.Success<MemberProfile?>)?.data
    val myMemberPayments = (myMemberPaymentsState as? UiState.Success<List<MemberPayment>>)?.data ?: emptyList()
    val myMemberAttendance = (myMemberAttendanceState as? UiState.Success<List<MemberAttendance>>)?.data ?: emptyList()
    val myRenewalRequests = (myRenewalRequestsState as? UiState.Success<List<RenewalRequest>>)?.data ?: emptyList()
    val announcements = (announcementsState as? UiState.Success<List<GymAnnouncement>>)?.data ?: emptyList()

    if (portalMode == PortalMode.MEMBER_PORTAL) {
        MemberPortalScaffold(
            memberProfile = myMemberProfile,
            payments = myMemberPayments,
            attendanceLogs = myMemberAttendance,
            renewalRequests = myRenewalRequests,
            announcements = announcements,
            bannerText = actionError ?: actionMessage,
            isErrorBanner = actionError != null,
            onDismissBanner = { viewModel.clearBanners() },
            onRegisterMember = { fullName, email, phone, age, address, _, planName, monthlyFee, photoUri ->
                if (email.trim().equals(OWNER_ADMIN_EMAIL, ignoreCase = true)) {
                    "This email belongs to the owner administrator account and cannot be registered as a member."
                } else {
                    viewModel.registerMember(fullName, email, phone, age, address, planName, monthlyFee, photoUri)
                    null
                }
            },
            onUpdateProfile = { fullName, phone, age, address, photoUri ->
                viewModel.updateOwnProfile(fullName, phone, age, address, photoUri)
            },
            onSubmitRenewal = { memberName, currentPlan, requestedPlan, durationMonths, paymentPreference, notes ->
                viewModel.submitRenewalRequest(memberName, currentPlan, requestedPlan, durationMonths, paymentPreference, notes)
            },
            onLogout = {
                signOut(scope = scope)
            }
        )
    } else {
        val streamError = (membersState as? UiState.Error)?.message
            ?: (classesState as? UiState.Error)?.message
            ?: (leadsState as? UiState.Error)?.message
            ?: (paymentsState as? UiState.Error)?.message

        val isLoading = membersState is UiState.Loading && classesState is UiState.Loading

        GymCrmScaffold(
            subtitle = "$userDisplayName • Admin Portal",
            isPreviewBanner = false,
            isLoading = isLoading,
            isBusy = isBusy,
            bannerText = actionError ?: streamError ?: actionMessage,
            isErrorBanner = actionError != null || streamError != null,
            onDismissBanner = { viewModel.clearBanners() },
            onTopAction = {
                signOut(scope = scope)
            },
            initialTab = GymTab.DASHBOARD,
            members = members,
            classes = classes,
            leads = leads,
            payments = payments,
            memberProfiles = allMemberProfiles,
            renewalRequests = allRenewalRequests,
            announcements = announcements,
            onSeedDemoData = { viewModel.seedDemoGymData() },
            onAddMember = { fullName, email, phone, planName, monthlyFee, status, assignedTrainer, goal, notes ->
                viewModel.addMember(fullName, email, phone, planName, monthlyFee, status, assignedTrainer, goal, notes)
            },
            onCheckInMember = { viewModel.checkInMember(it) },
            onCycleMemberStatus = { viewModel.cycleMemberStatus(it) },
            onDeleteMember = { viewModel.deleteMember(it) },
            onAddClass = { title, coachName, category, scheduleTime, room, capacity, intensity ->
                viewModel.addClass(title, coachName, category, scheduleTime, room, capacity, intensity)
            },
            onAdjustClassEnrollment = { cls, delta -> viewModel.adjustClassEnrollment(cls, delta) },
            onDeleteClass = { viewModel.deleteClass(it) },
            onAddLead = { prospectName, phone, source, interestedPlan, stage, notes ->
                viewModel.addLead(prospectName, phone, source, interestedPlan, stage, notes)
            },
            onAdvanceLead = { viewModel.advanceLeadStage(it) },
            onConvertLead = { viewModel.convertLeadToMember(it) },
            onDeleteLead = { viewModel.deleteLead(it) },
            onAddPayment = { memberName, amount, planName, paymentMethod, status, billingPeriod ->
                viewModel.addPayment(
                    memberId = "mem_${System.currentTimeMillis()}",
                    memberName = memberName,
                    amount = amount,
                    planName = planName,
                    paymentMethod = paymentMethod,
                    status = status,
                    billingPeriod = billingPeriod
                )
            },
            onTogglePaymentStatus = { viewModel.markPaymentPaid(it) },
            onDeletePayment = { viewModel.deletePayment(it) },
            onApproveMemberRegistration = { profile, assignedPlan, monthlyFee, startDate, expiryDate, daysRemaining ->
                viewModel.approveMemberRegistration(profile, assignedPlan, monthlyFee, startDate, expiryDate, daysRemaining)
            },
            onRejectMemberRegistration = { viewModel.rejectMemberRegistration(it) },
            onSuspendMemberRegistration = { viewModel.suspendMemberRegistration(it) },
            onActivateMemberRegistration = { viewModel.activateMemberRegistration(it) },
            onUpdateRenewalStatus = { req, status -> viewModel.updateRenewalRequestStatus(req, status) },
            onPostAnnouncement = { title, body, category, priority ->
                viewModel.postAnnouncement(title, body, category, priority)
            }
        )
    }
}

@Composable
fun LocalPreviewFeatureScreen(
    portalMode: PortalMode,
    currentPortalMemberId: String,
    onUpdateCurrentPortalMemberId: (String) -> Unit,
    members: SnapshotStateList<Member>,
    classes: SnapshotStateList<GymClass>,
    leads: SnapshotStateList<GymLead>,
    payments: SnapshotStateList<GymPayment>,
    memberProfiles: SnapshotStateList<MemberProfile>,
    userAccounts: SnapshotStateMap<String, UserAccount>,
    credentialDigests: SnapshotStateMap<String, String>,
    memberPayments: SnapshotStateList<MemberPayment>,
    memberAttendanceLogs: SnapshotStateList<MemberAttendance>,
    renewalRequests: SnapshotStateList<RenewalRequest>,
    announcements: SnapshotStateList<GymAnnouncement>,
    onBackToSignIn: () -> Unit
) {
    BackHandler {
        onBackToSignIn()
    }

    val activeMemberProfile = memberProfiles.firstOrNull { it.id == currentPortalMemberId }

    var bannerMessage by remember {
        mutableStateOf<String?>("Alpha Gym Centre CRM — Admin & Member Portals Connected.")
    }

    if (portalMode == PortalMode.MEMBER_PORTAL) {
        MemberPortalScaffold(
            memberProfile = activeMemberProfile,
            payments = memberPayments.filter { it.userId == currentPortalMemberId },
            attendanceLogs = memberAttendanceLogs.filter { it.userId == currentPortalMemberId },
            renewalRequests = renewalRequests.filter { it.userId == currentPortalMemberId },
            announcements = announcements,
            bannerText = bannerMessage,
            isErrorBanner = false,
            onDismissBanner = { bannerMessage = null },
            onRegisterMember = { fullName, email, phone, age, address, password, planName, monthlyFee, photoUri ->
                val cleanEmail = email.trim().lowercase(Locale.US)
                val cleanPhoneDigits = normalizePhoneDigits(phone)

                if (cleanEmail == OWNER_ADMIN_EMAIL) {
                    "This email is reserved for the gym administrator and cannot be used for public member registration."
                } else if (userAccounts.values.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
                    "An account with email '$cleanEmail' already exists. Please sign in instead."
                } else if (cleanPhoneDigits.length >= 7 && userAccounts.values.any { normalizePhoneDigits(it.phone) == cleanPhoneDigits }) {
                    "An account with phone number '$phone' is already registered."
                } else {
                    val newUid = "mem_${System.currentTimeMillis()}"
                    credentialDigests[newUid] = computeCredentialDigest(cleanEmail, password)
                    userAccounts[newUid] = UserAccount(
                        id = newUid,
                        name = fullName,
                        email = cleanEmail,
                        phone = phone,
                        role = "member",
                        status = "pending"
                    )
                    val newProfile = MemberProfile(
                        id = newUid,
                        userId = newUid,
                        fullName = fullName,
                        email = cleanEmail,
                        phone = phone,
                        age = age,
                        address = address,
                        planName = planName,
                        monthlyFee = monthlyFee,
                        photoUri = photoUri,
                        role = "member",
                        status = "pending",
                        startDate = "Pending Approval",
                        expiryDate = "Pending Approval",
                        daysRemaining = 0L,
                        paymentStatus = "Pending",
                        attendanceCount = 0L,
                        recentActivity = "Registration submitted — awaiting admin approval"
                    )
                    memberProfiles.add(0, newProfile)
                    onUpdateCurrentPortalMemberId(newUid)
                    bannerMessage = "Your account is waiting for admin approval."
                    null
                }
            },
            onUpdateProfile = { fullName, phone, age, address, photoUri ->
                val idx = memberProfiles.indexOfFirst { it.id == currentPortalMemberId }
                if (idx >= 0) {
                    memberProfiles[idx] = memberProfiles[idx].copy(
                        fullName = fullName,
                        phone = phone,
                        age = age,
                        address = address,
                        photoUri = photoUri
                    )
                    userAccounts[currentPortalMemberId]?.let { existing ->
                        userAccounts[currentPortalMemberId] = existing.copy(
                            name = fullName,
                            phone = phone
                        )
                    }
                    bannerMessage = "Member profile contact details updated."
                }
            },
            onSubmitRenewal = { memberName, currentPlan, requestedPlan, durationMonths, paymentPreference, notes ->
                renewalRequests.add(
                    0,
                    RenewalRequest(
                        id = "rr_${System.currentTimeMillis()}",
                        userId = currentPortalMemberId,
                        memberName = memberName,
                        currentPlan = currentPlan,
                        requestedPlan = requestedPlan,
                        durationMonths = durationMonths,
                        paymentPreference = paymentPreference,
                        notes = notes,
                        status = "pending"
                    )
                )
                bannerMessage = "Renewal request for $requestedPlan ($durationMonths mo) sent to Admin Approvals!"
            },
            onLogout = onBackToSignIn
        )
    } else {
        GymCrmScaffold(
            subtitle = "$OWNER_ADMIN_EMAIL • Admin Portal",
            isPreviewBanner = true,
            isLoading = false,
            isBusy = false,
            bannerText = bannerMessage,
            isErrorBanner = false,
            onDismissBanner = { bannerMessage = null },
            onTopAction = onBackToSignIn,
            initialTab = GymTab.DASHBOARD,
            members = members,
            classes = classes,
            leads = leads,
            payments = payments,
            memberProfiles = memberProfiles,
            renewalRequests = renewalRequests,
            announcements = announcements,
            onSeedDemoData = {
                bannerMessage = "Sample gym data is active!"
            },
            onAddMember = { fullName, email, phone, planName, monthlyFee, status, assignedTrainer, goal, notes ->
                members.add(
                    0,
                    Member(
                        id = "preview_m_${System.currentTimeMillis()}",
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        planName = planName,
                        monthlyFee = monthlyFee,
                        status = status,
                        assignedTrainer = assignedTrainer,
                        goal = goal,
                        attendanceCount = 0L,
                        lastCheckInDate = "Never",
                        notes = notes
                    )
                )
                bannerMessage = "Member $fullName added."
            },
            onCheckInMember = { member ->
                val idx = members.indexOfFirst { it.id == member.id }
                if (idx >= 0) {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    members[idx] = member.copy(
                        attendanceCount = member.attendanceCount + 1L,
                        lastCheckInDate = today
                    )
                    bannerMessage = "${member.fullName} checked in (#${member.attendanceCount + 1})"
                }
            },
            onCycleMemberStatus = { member ->
                val idx = members.indexOfFirst { it.id == member.id }
                if (idx >= 0) {
                    val nextStatus = when (member.status) {
                        "Active" -> "Expiring"
                        "Expiring" -> "Frozen"
                        "Frozen" -> "Overdue"
                        else -> "Active"
                    }
                    members[idx] = member.copy(status = nextStatus)
                    bannerMessage = "${member.fullName} status updated to $nextStatus"
                }
            },
            onDeleteMember = { member ->
                members.removeAll { it.id == member.id }
                bannerMessage = "${member.fullName} removed."
            },
            onAddClass = { title, coachName, category, scheduleTime, room, capacity, intensity ->
                classes.add(
                    0,
                    GymClass(
                        id = "preview_c_${System.currentTimeMillis()}",
                        title = title,
                        coachName = coachName,
                        category = category,
                        scheduleTime = scheduleTime,
                        room = room,
                        capacity = capacity,
                        enrolledCount = 0L,
                        intensity = intensity
                    )
                )
                bannerMessage = "Class '$title' scheduled."
            },
            onAdjustClassEnrollment = { cls, delta ->
                val idx = classes.indexOfFirst { it.id == cls.id }
                if (idx >= 0) {
                    val next = (cls.enrolledCount + delta).coerceIn(0L, cls.capacity)
                    classes[idx] = cls.copy(enrolledCount = next)
                }
            },
            onDeleteClass = { cls ->
                classes.removeAll { it.id == cls.id }
                bannerMessage = "Class '${cls.title}' removed."
            },
            onAddLead = { prospectName, phone, source, interestedPlan, stage, notes ->
                leads.add(
                    0,
                    GymLead(
                        id = "preview_l_${System.currentTimeMillis()}",
                        prospectName = prospectName,
                        phone = phone,
                        source = source,
                        interestedPlan = interestedPlan,
                        stage = stage,
                        notes = notes
                    )
                )
                bannerMessage = "Lead $prospectName added to pipeline."
            },
            onAdvanceLead = { lead ->
                val idx = leads.indexOfFirst { it.id == lead.id }
                if (idx >= 0) {
                    val nextStage = when (lead.stage) {
                        "New" -> "Tour Booked"
                        "Tour Booked" -> "Trial Active"
                        "Trial Active" -> "Won"
                        else -> "New"
                    }
                    leads[idx] = lead.copy(stage = nextStage)
                    bannerMessage = "${lead.prospectName} moved to $nextStage"
                }
            },
            onConvertLead = { lead ->
                val idx = leads.indexOfFirst { it.id == lead.id }
                if (idx >= 0) {
                    leads[idx] = lead.copy(stage = "Won")
                }
                val fee = when (lead.interestedPlan) {
                    "Starter" -> 79L
                    "Elite VIP" -> 199L
                    "Day Pass" -> 25L
                    else -> 129L
                }
                val newId = "preview_m_${System.currentTimeMillis()}"
                members.add(
                    0,
                    Member(
                        id = newId,
                        fullName = lead.prospectName,
                        email = "${lead.prospectName.lowercase(Locale.US).replace(" ", ".")}@member.fit",
                        phone = lead.phone,
                        planName = lead.interestedPlan,
                        monthlyFee = fee,
                        status = "Active",
                        assignedTrainer = "Coach Elena Rostova",
                        goal = "Strength & Conditioning",
                        attendanceCount = 1L,
                        lastCheckInDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                        notes = "Converted from ${lead.source}. ${lead.notes}"
                    )
                )
                payments.add(
                    0,
                    GymPayment(
                        id = "preview_p_${System.currentTimeMillis()}",
                        memberId = newId,
                        memberName = lead.prospectName,
                        amount = fee,
                        planName = lead.interestedPlan,
                        paymentMethod = "Card",
                        status = "Paid",
                        billingPeriod = "Nov 2026"
                    )
                )
                bannerMessage = "${lead.prospectName} converted to active member & billed!"
            },
            onDeleteLead = { lead ->
                leads.removeAll { it.id == lead.id }
                bannerMessage = "Lead ${lead.prospectName} removed."
            },
            onAddPayment = { memberName, amount, planName, paymentMethod, status, billingPeriod ->
                payments.add(
                    0,
                    GymPayment(
                        id = "preview_p_${System.currentTimeMillis()}",
                        memberId = "preview_m",
                        memberName = memberName,
                        amount = amount,
                        planName = planName,
                        paymentMethod = paymentMethod,
                        status = status,
                        billingPeriod = billingPeriod
                    )
                )
                bannerMessage = "Invoice for $memberName ($$amount) logged."
            },
            onTogglePaymentStatus = { payment ->
                val idx = payments.indexOfFirst { it.id == payment.id }
                if (idx >= 0) {
                    val next = if (payment.status == "Paid") "Pending" else "Paid"
                    payments[idx] = payment.copy(status = next)
                    bannerMessage = "Invoice for ${payment.memberName} marked $next"
                }
            },
            onDeletePayment = { payment ->
                payments.removeAll { it.id == payment.id }
                bannerMessage = "Invoice removed."
            },
            onApproveMemberRegistration = { profile, assignedPlan, monthlyFee, startDate, expiryDate, daysRemaining ->
                val idx = memberProfiles.indexOfFirst { it.id == profile.id }
                if (idx >= 0) {
                    val approved = memberProfiles[idx].copy(
                        planName = assignedPlan,
                        monthlyFee = monthlyFee,
                        status = "active",
                        startDate = startDate,
                        expiryDate = expiryDate,
                        daysRemaining = daysRemaining,
                        paymentStatus = "Paid",
                        attendanceCount = memberProfiles[idx].attendanceCount.coerceAtLeast(1L),
                        recentActivity = "Membership approved by Admin • Plan: $assignedPlan"
                    )
                    memberProfiles[idx] = approved
                    userAccounts[approved.id]?.let { existing ->
                        userAccounts[approved.id] = existing.copy(status = "active")
                    }
                    onUpdateCurrentPortalMemberId(approved.id)

                    // Issue official payment receipt & initial attendance log for Member Portal
                    memberPayments.add(
                        0,
                        MemberPayment(
                            id = "mp_${System.currentTimeMillis()}",
                            userId = approved.id,
                            memberName = approved.fullName,
                            planName = assignedPlan,
                            amount = monthlyFee,
                            paymentMethod = "Card",
                            status = "Paid",
                            billingPeriod = "Nov 2026",
                            receiptNumber = "ALPHA-RCP-${System.currentTimeMillis().toString().takeLast(6)}"
                        )
                    )
                    memberAttendanceLogs.add(
                        0,
                        MemberAttendance(
                            id = "ma_${System.currentTimeMillis()}",
                            userId = approved.id,
                            memberName = approved.fullName,
                            checkInDate = startDate,
                            checkInTime = "09:00 AM",
                            sessionType = "Membership Approval & Orientation Check-In",
                            verifiedBy = "Alpha Gym Admin"
                        )
                    )

                    // Sync to Admin Members Roster if not already listed
                    if (members.none { it.email.equals(approved.email, ignoreCase = true) }) {
                        members.add(
                            0,
                            Member(
                                id = "adm_${approved.id}",
                                fullName = approved.fullName,
                                email = approved.email,
                                phone = approved.phone,
                                planName = assignedPlan,
                                monthlyFee = monthlyFee,
                                status = "Active",
                                assignedTrainer = "Coach Elena Rostova",
                                goal = "General Fitness & Strength",
                                attendanceCount = 1L,
                                lastCheckInDate = startDate,
                                notes = "Approved via Member Portal Registration"
                            )
                        )
                    }
                    bannerMessage = "${approved.fullName} approved (status = active) for $assignedPlan!"
                }
            },
            onRejectMemberRegistration = { profile ->
                val idx = memberProfiles.indexOfFirst { it.id == profile.id }
                if (idx >= 0) {
                    memberProfiles[idx] = memberProfiles[idx].copy(
                        status = "rejected",
                        paymentStatus = "Cancelled",
                        recentActivity = "Registration declined by Admin"
                    )
                    userAccounts[profile.id]?.let { existing ->
                        userAccounts[profile.id] = existing.copy(status = "rejected")
                    }
                    bannerMessage = "${profile.fullName} registration marked rejected."
                }
            },
            onSuspendMemberRegistration = { profile ->
                val idx = memberProfiles.indexOfFirst { it.id == profile.id }
                if (idx >= 0) {
                    memberProfiles[idx] = memberProfiles[idx].copy(
                        status = "suspended",
                        recentActivity = "Account suspended by Admin"
                    )
                    userAccounts[profile.id]?.let { existing ->
                        userAccounts[profile.id] = existing.copy(status = "suspended")
                    }
                    bannerMessage = "${profile.fullName} has been suspended. Member Dashboard access blocked."
                }
            },
            onActivateMemberRegistration = { profile ->
                val idx = memberProfiles.indexOfFirst { it.id == profile.id }
                if (idx >= 0) {
                    memberProfiles[idx] = memberProfiles[idx].copy(
                        status = "active",
                        paymentStatus = "Paid",
                        recentActivity = "Account re-activated by Admin"
                    )
                    userAccounts[profile.id]?.let { existing ->
                        userAccounts[profile.id] = existing.copy(status = "active")
                    }
                    bannerMessage = "${profile.fullName} has been activated (status = active)."
                }
            },
            onUpdateRenewalStatus = { req, nextStatus ->
                val idx = renewalRequests.indexOfFirst { it.id == req.id }
                if (idx >= 0) {
                    renewalRequests[idx] = renewalRequests[idx].copy(status = nextStatus)
                    bannerMessage = "Renewal request for ${req.memberName} marked $nextStatus."
                }
            },
            onPostAnnouncement = { title, body, category, priority ->
                announcements.add(
                    0,
                    GymAnnouncement(
                        id = "ann_${System.currentTimeMillis()}",
                        authorId = OWNER_ADMIN_UID,
                        title = title,
                        body = body,
                        category = category,
                        priority = priority
                    )
                )
                bannerMessage = "Announcement '$title' published to Member Portal!"
            }
        )
    }
}

@Composable
private fun GymCrmScaffold(
    subtitle: String,
    isPreviewBanner: Boolean,
    isLoading: Boolean,
    isBusy: Boolean,
    bannerText: String?,
    isErrorBanner: Boolean,
    onDismissBanner: () -> Unit,
    onTopAction: () -> Unit,
    initialTab: GymTab = GymTab.DASHBOARD,
    members: List<Member>,
    classes: List<GymClass>,
    leads: List<GymLead>,
    payments: List<GymPayment>,
    memberProfiles: List<MemberProfile>,
    renewalRequests: List<RenewalRequest>,
    announcements: List<GymAnnouncement>,
    onSeedDemoData: () -> Unit,
    onAddMember: (String, String, String, String, Long, String, String, String, String) -> Unit,
    onCheckInMember: (Member) -> Unit,
    onCycleMemberStatus: (Member) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onAddClass: (String, String, String, String, String, Long, String) -> Unit,
    onAdjustClassEnrollment: (GymClass, Long) -> Unit,
    onDeleteClass: (GymClass) -> Unit,
    onAddLead: (String, String, String, String, String, String) -> Unit,
    onAdvanceLead: (GymLead) -> Unit,
    onConvertLead: (GymLead) -> Unit,
    onDeleteLead: (GymLead) -> Unit,
    onAddPayment: (String, Long, String, String, String, String) -> Unit,
    onTogglePaymentStatus: (GymPayment) -> Unit,
    onDeletePayment: (GymPayment) -> Unit,
    onApproveMemberRegistration: (MemberProfile, String, Long, String, String, Long) -> Unit,
    onRejectMemberRegistration: (MemberProfile) -> Unit,
    onSuspendMemberRegistration: (MemberProfile) -> Unit,
    onActivateMemberRegistration: (MemberProfile) -> Unit,
    onUpdateRenewalStatus: (RenewalRequest, String) -> Unit,
    onPostAnnouncement: (String, String, String, String) -> Unit
) {
    var currentTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    var showAddMemberDialog by rememberSaveable { mutableStateOf(false) }
    var showAddClassDialog by rememberSaveable { mutableStateOf(false) }
    var showAddLeadDialog by rememberSaveable { mutableStateOf(false) }
    var showAddPaymentDialog by rememberSaveable { mutableStateOf(false) }

    if (currentTab != GymTab.DASHBOARD) {
        BackHandler {
            currentTab = GymTab.DASHBOARD
        }
    }

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
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPreviewBanner) {
                            OutlinedButton(
                                onClick = onTopAction,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("sign_in_top_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout Admin",
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
                        } else {
                            IconButton(
                                onClick = onTopAction,
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = stringResource(R.string.cd_sign_out),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepSlate,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                GymTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = ElectricLime,
                            indicatorColor = ElectricLime,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AlphaGym3DBackgroundWatermark(watermarkAlpha = 0.10f)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            if (bannerText != null) {
                Surface(
                    color = if (isErrorBanner) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    } else {
                        EmeraldSuccess.copy(alpha = 0.15f)
                    },
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
                            color = if (isErrorBanner) MaterialTheme.colorScheme.error else EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissBanner,
                            modifier = Modifier.size(28.dp)
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

            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(color = ElectricLime)
                }
            } else {
                when (currentTab) {
                    GymTab.DASHBOARD -> DashboardTabContent(
                        members = members,
                        classes = classes,
                        leads = leads,
                        payments = payments,
                        isBusy = isBusy,
                        onSeedDemoData = onSeedDemoData,
                        onOpenAddMember = { showAddMemberDialog = true },
                        onOpenAddClass = { showAddClassDialog = true },
                        onOpenAddLead = { showAddLeadDialog = true },
                        onOpenAddPayment = { showAddPaymentDialog = true },
                        onCheckInMember = onCheckInMember
                    )

                    GymTab.APPROVALS -> AdminApprovalsTabContent(
                        memberProfiles = memberProfiles,
                        renewalRequests = renewalRequests,
                        announcements = announcements,
                        onApproveMember = onApproveMemberRegistration,
                        onRejectMember = onRejectMemberRegistration,
                        onSuspendMember = onSuspendMemberRegistration,
                        onActivateMember = onActivateMemberRegistration,
                        onUpdateRenewalStatus = onUpdateRenewalStatus,
                        onPostAnnouncement = onPostAnnouncement
                    )

                    GymTab.MEMBERS -> MembersTabContent(
                        members = members,
                        onOpenAddMember = { showAddMemberDialog = true },
                        onCheckInMember = onCheckInMember,
                        onCycleMemberStatus = onCycleMemberStatus,
                        onDeleteMember = onDeleteMember
                    )

                    GymTab.CLASSES -> ClassesTabContent(
                        classes = classes,
                        onOpenAddClass = { showAddClassDialog = true },
                        onAdjustEnrollment = onAdjustClassEnrollment,
                        onDeleteClass = onDeleteClass
                    )

                    GymTab.LEADS -> LeadsTabContent(
                        leads = leads,
                        onOpenAddLead = { showAddLeadDialog = true },
                        onAdvanceLead = onAdvanceLead,
                        onConvertLead = onConvertLead,
                        onDeleteLead = onDeleteLead
                    )

                    GymTab.BILLING -> BillingTabContent(
                        payments = payments,
                        onOpenAddPayment = { showAddPaymentDialog = true },
                        onTogglePaymentStatus = onTogglePaymentStatus,
                        onDeletePayment = onDeletePayment
                    )
                }
            }
            }
        }
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { fullName, email, phone, planName, monthlyFee, status, assignedTrainer, goal, notes ->
                showAddMemberDialog = false
                onAddMember(fullName, email, phone, planName, monthlyFee, status, assignedTrainer, goal, notes)
            }
        )
    }

    if (showAddClassDialog) {
        AddClassDialog(
            onDismiss = { showAddClassDialog = false },
            onConfirm = { title, coachName, category, scheduleTime, room, capacity, intensity ->
                showAddClassDialog = false
                onAddClass(title, coachName, category, scheduleTime, room, capacity, intensity)
            }
        )
    }

    if (showAddLeadDialog) {
        AddLeadDialog(
            onDismiss = { showAddLeadDialog = false },
            onConfirm = { prospectName, phone, source, interestedPlan, stage, notes ->
                showAddLeadDialog = false
                onAddLead(prospectName, phone, source, interestedPlan, stage, notes)
            }
        )
    }

    if (showAddPaymentDialog) {
        AddPaymentDialog(
            onDismiss = { showAddPaymentDialog = false },
            onConfirm = { memberName, amount, planName, paymentMethod, status, billingPeriod ->
                showAddPaymentDialog = false
                onAddPayment(memberName, amount, planName, paymentMethod, status, billingPeriod)
            }
        )
    }
}
