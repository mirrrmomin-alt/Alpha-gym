package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.GymAnnouncement
import com.example.data.model.MemberProfile
import com.example.data.model.RenewalRequest
import com.example.ui.theme.AlphaRed
import com.example.ui.theme.BlazeAmber
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AdminApprovalsTabContent(
    memberProfiles: List<MemberProfile>,
    renewalRequests: List<RenewalRequest>,
    announcements: List<GymAnnouncement>,
    onApproveMember: (
        profile: MemberProfile,
        assignedPlan: String,
        monthlyFee: Long,
        startDate: String,
        expiryDate: String,
        daysRemaining: Long
    ) -> Unit,
    onRejectMember: (MemberProfile) -> Unit,
    onSuspendMember: (MemberProfile) -> Unit = {},
    onActivateMember: (MemberProfile) -> Unit = {},
    onUpdateRenewalStatus: (RenewalRequest, String) -> Unit,
    onPostAnnouncement: (title: String, body: String, category: String, priority: String) -> Unit
) {
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var showAnnouncementDialog by rememberSaveable { mutableStateOf(false) }
    var memberToConfigureApproval by remember { mutableStateOf<MemberProfile?>(null) }

    val filters = listOf("All", "Pending", "Active", "Suspended", "Rejected", "Renewals", "Announcements")
    val pendingCount = memberProfiles.count { it.status.equals("pending", ignoreCase = true) }
    val pendingRenewalsCount = renewalRequests.count { it.status.equals("pending", ignoreCase = true) }

    val filteredProfiles = memberProfiles.filter { profile ->
        when (selectedFilter) {
            "Pending" -> profile.status.equals("pending", ignoreCase = true)
            "Active" -> profile.status.equals("active", ignoreCase = true) || profile.status.equals("approved", ignoreCase = true)
            "Suspended" -> profile.status.equals("suspended", ignoreCase = true)
            "Rejected" -> profile.status.equals("rejected", ignoreCase = true)
            else -> true
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_approvals_list")
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlphaRed.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Member Approvals & Portal Sync",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$pendingCount Pending Registrations • $pendingRenewalsCount Renewal Requests",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAnnouncementDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AlphaRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("admin_post_announcement_btn")
                        ) {
                            Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Announce", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                filters.forEach { filter ->
                    val badgeText = when (filter) {
                        "Pending" -> if (pendingCount > 0) "Pending ($pendingCount)" else "Pending"
                        "Renewals" -> if (pendingRenewalsCount > 0) "Renewals ($pendingRenewalsCount)" else "Renewals"
                        else -> filter
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(badgeText) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AlphaRed.copy(alpha = 0.15f),
                            selectedLabelColor = AlphaRed
                        ),
                        modifier = Modifier.testTag("approvals_filter_${filter.lowercase(Locale.US)}")
                    )
                }
            }
        }

        if (selectedFilter == "Renewals") {
            if (renewalRequests.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Membership Renewal Requests",
                        subtitle = "When members submit plan upgrades or renewals in the Member Portal, they appear here."
                    )
                }
            } else {
                items(renewalRequests, key = { it.id }) { request ->
                    AdminRenewalRequestCard(
                        request = request,
                        onApprove = { onUpdateRenewalStatus(request, "approved") },
                        onReject = { onUpdateRenewalStatus(request, "rejected") }
                    )
                }
            }
        } else if (selectedFilter == "Announcements") {
            if (announcements.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Gym Announcements Published",
                        subtitle = "Tap 'Announce' above to broadcast news, holiday hours, or challenges to all members."
                    )
                }
            } else {
                items(announcements, key = { it.id }) { announcement ->
                    AdminAnnouncementCard(announcement = announcement)
                }
            }
        } else {
            if (filteredProfiles.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Member Registrations in '$selectedFilter'",
                        subtitle = "New registrations from the Member Portal appear here with status = pending for Admin review."
                    )
                }
            } else {
                items(filteredProfiles, key = { it.id.ifBlank { it.userId } }) { profile ->
                    MemberApprovalCard(
                        profile = profile,
                        onQuickApprove = {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
                            val expiry = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                            onApproveMember(
                                profile,
                                profile.planName,
                                profile.monthlyFee,
                                today,
                                expiry,
                                30L
                            )
                        },
                        onCustomizeAndApprove = { memberToConfigureApproval = profile },
                        onReject = { onRejectMember(profile) },
                        onSuspend = { onSuspendMember(profile) },
                        onActivate = { onActivateMember(profile) }
                    )
                }
            }
        }
    }

    if (memberToConfigureApproval != null) {
        val target = memberToConfigureApproval!!
        ApproveMemberAssignmentDialog(
            profile = target,
            onDismiss = { memberToConfigureApproval = null },
            onConfirmApproval = { plan, fee, start, expiry, days ->
                memberToConfigureApproval = null
                onApproveMember(target, plan, fee, start, expiry, days)
            }
        )
    }

    if (showAnnouncementDialog) {
        PostAnnouncementDialog(
            onDismiss = { showAnnouncementDialog = false },
            onConfirm = { title, body, category, priority ->
                showAnnouncementDialog = false
                onPostAnnouncement(title, body, category, priority)
            }
        )
    }
}

@Composable
private fun MemberApprovalCard(
    profile: MemberProfile,
    onQuickApprove: () -> Unit,
    onCustomizeAndApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onActivate: () -> Unit
) {
    val isPending = profile.status.equals("pending", ignoreCase = true)
    val isActive = profile.status.equals("active", ignoreCase = true) || profile.status.equals("approved", ignoreCase = true)
    val isSuspended = profile.status.equals("suspended", ignoreCase = true)
    val badgeColor = when {
        isActive -> EmeraldSuccess
        isPending -> BlazeAmber
        else -> CrimsonPulse
    }
    val cardId = profile.id.ifBlank { profile.userId }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .testTag("approval_card_$cardId")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AlphaRed.copy(alpha = 0.12f))
                            .border(1.dp, AlphaRed.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = AlphaRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Requested Plan: ${profile.planName} ($${profile.monthlyFee}/mo) • Age ${profile.age}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        text = profile.status.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSuspended) {
                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("activate_member_btn_$cardId")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activate Member", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onQuickApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_member_btn_$cardId")
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isActive) "Re-Approve (30d)" else "Approve Member", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onCustomizeAndApprove,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("assign_plan_dates_btn_$cardId")
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = AlphaRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Plan & Dates", color = AlphaRed)
                }

                if (isActive) {
                    OutlinedButton(
                        onClick = onSuspend,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("suspend_member_btn_$cardId")
                    ) {
                        Text("Suspend", color = CrimsonPulse, fontWeight = FontWeight.Bold)
                    }
                } else if (!profile.status.equals("rejected", ignoreCase = true)) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("reject_member_btn_$cardId")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Reject", tint = CrimsonPulse, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Member Registration Details
            Surface(
                color = ElevatedSlate.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${profile.email}  •  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = profile.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Home, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Address: ${profile.address} • Role: ${profile.role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = AlphaRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Start: ${profile.startDate} • Expiry: ${profile.expiryDate} (${profile.daysRemaining}d left)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApproveMemberAssignmentDialog(
    profile: MemberProfile,
    onDismiss: () -> Unit,
    onConfirmApproval: (
        assignedPlan: String,
        monthlyFee: Long,
        startDate: String,
        expiryDate: String,
        daysRemaining: Long
    ) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val defaultExpiry = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    var selectedPlan by remember { mutableStateOf(profile.planName) }
    var feeText by remember { mutableStateOf(profile.monthlyFee.toString()) }
    var startDate by remember { mutableStateOf(if (profile.startDate == "Pending Approval") today else profile.startDate) }
    var expiryDate by remember { mutableStateOf(if (profile.expiryDate == "Pending Approval") defaultExpiry else profile.expiryDate) }
    var daysRemainingText by remember { mutableStateOf(if (profile.daysRemaining <= 0L) "30" else profile.daysRemaining.toString()) }

    val plans = listOf(
        "Starter" to "79",
        "Pro Athlete" to "129",
        "Elite VIP" to "199",
        "Day Pass" to "25"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = { Text("Assign Plan & Membership Dates") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Approving: ${profile.fullName} (${profile.email})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Assign Membership Plan", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    plans.forEach { (plan, price) ->
                        FilterChip(
                            selected = selectedPlan == plan,
                            onClick = {
                                selectedPlan = plan
                                feeText = price
                            },
                            label = { Text("$plan ($$price)") }
                        )
                    }
                }
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Membership Fee (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Expiry Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysRemainingText,
                    onValueChange = { daysRemainingText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Days Remaining") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmApproval(
                        selectedPlan,
                        feeText.toLongOrNull() ?: 129L,
                        startDate.ifBlank { today },
                        expiryDate.ifBlank { defaultExpiry },
                        daysRemainingText.toLongOrNull() ?: 30L
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_approve_assignment_btn")
            ) {
                Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Approve & Activate", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AdminRenewalRequestCard(
    request: RenewalRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isPending = request.status.equals("pending", ignoreCase = true)
    val statusColor = when (request.status.lowercase(Locale.US)) {
        "approved" -> EmeraldSuccess
        "rejected" -> CrimsonPulse
        else -> BlazeAmber
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Autorenew, contentDescription = null, tint = AlphaRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = request.memberName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${request.currentPlan} → ${request.requestedPlan} (${request.durationMonths} Months)",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan
                        )
                    }
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        text = request.status.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            if (request.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${request.notes} • Prefers: ${request.paymentPreference}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Approve Renewal", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Decline", color = CrimsonPulse)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAnnouncementCard(announcement: GymAnnouncement) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = AlphaRed.copy(alpha = 0.14f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${announcement.category} • ${announcement.priority}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AlphaRed,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = announcement.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostAnnouncementDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, body: String, category: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Schedule & Events") }
    var priority by remember { mutableStateOf("Important") }

    val priorities = listOf("Normal", "Important", "Urgent")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = { Text("Publish Gym Announcement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Headline *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Priority Level")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Announcement Message *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title.ifBlank { "Alpha Gym Equipment Upgrade" },
                        body.ifBlank { "New competition power racks and calibrated plates are now open on Floor 1." },
                        category.ifBlank { "General" },
                        priority
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlphaRed,
                    contentColor = Color.White
                )
            ) {
                Text("Publish", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
