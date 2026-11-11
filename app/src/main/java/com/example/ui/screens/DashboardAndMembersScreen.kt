package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.GymClass
import com.example.data.model.GymLead
import com.example.data.model.GymPayment
import com.example.data.model.Member
import com.example.ui.theme.BlazeAmber
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianSlate

@Composable
fun DashboardTabContent(
    members: List<Member>,
    classes: List<GymClass>,
    leads: List<GymLead>,
    payments: List<GymPayment>,
    isBusy: Boolean,
    onSeedDemoData: () -> Unit,
    onOpenAddMember: () -> Unit,
    onOpenAddClass: () -> Unit,
    onOpenAddLead: () -> Unit,
    onOpenAddPayment: () -> Unit,
    onCheckInMember: (Member) -> Unit
) {
    val activeMembersCount = members.count { it.status == "Active" }
    val mrr = members.filter { it.status == "Active" || it.status == "Expiring" }.sumOf { it.monthlyFee }
    val totalCheckIns = members.sumOf { it.attendanceCount }
    val openLeadsCount = leads.count { it.stage != "Won" && it.stage != "Lost" }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_list")
    ) {
        // Hero Banner Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_gym_hero_1794301907512),
                        contentDescription = "IronPulse Studio Floor",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ObsidianSlate.copy(alpha = 0.94f),
                                        ObsidianSlate.copy(alpha = 0.72f),
                                        ObsidianSlate.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = ElectricLime.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "LIVE TELEMETRY • CLOUD SYNCED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricLime,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "${classes.size} Classes Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "IronPulse Performance Hub",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track member retention, class capacity, and recurring studio revenue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            QuickActionChip(
                                label = "+ Member",
                                color = ElectricLime,
                                onClick = onOpenAddMember,
                                tag = "quick_add_member_btn"
                            )
                            QuickActionChip(
                                label = "+ Class",
                                color = CyberCyan,
                                onClick = onOpenAddClass,
                                tag = "quick_add_class_btn"
                            )
                            QuickActionChip(
                                label = "+ Lead",
                                color = BlazeAmber,
                                onClick = onOpenAddLead,
                                tag = "quick_add_lead_btn"
                            )
                            QuickActionChip(
                                label = "+ Invoice",
                                color = EmeraldSuccess,
                                onClick = onOpenAddPayment,
                                tag = "quick_add_invoice_btn"
                            )
                        }
                    }
                }
            }
        }

        // Empty-State Starter Data Prompt if no members exist yet
        if (members.isEmpty() && classes.isEmpty() && leads.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElevatedSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricLime.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricLime
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Populate Your Studio CRM",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your cloud database is ready. Load a complete set of sample athletes, strength & HIIT classes, prospective leads, and invoices with one tap, or create records manually.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onSeedDemoData,
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = ObsidianSlate
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("seed_demo_data_button")
                        ) {
                            Text(
                                text = if (isBusy) "Loading Studio Data..." else "Load Starter Gym Data",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2x2 KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    KpiStatCard(
                        title = "Active Athletes",
                        value = "$activeMembersCount / ${members.size}",
                        subtitle = "Roster retention",
                        icon = Icons.Filled.Groups,
                        accent = ElectricLime,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Monthly MRR",
                        value = "$$mrr",
                        subtitle = "${payments.count { it.status == "Paid" }} paid invoices",
                        icon = Icons.Filled.AttachMoney,
                        accent = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    KpiStatCard(
                        title = "Floor Check-Ins",
                        value = "$totalCheckIns",
                        subtitle = "Cumulative visits",
                        icon = Icons.Filled.HowToReg,
                        accent = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Active Leads",
                        value = "$openLeadsCount",
                        subtitle = "${leads.count { it.stage == "Won" }} converted",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accent = BlazeAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Custom Canvas Membership Tier Distribution Chart
        item {
            MembershipTierAnalyticsCard(members = members)
        }

        // Express Check-In Roster Preview
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Express Floor Check-In",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${members.size} total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (members.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Athletes Registered Yet",
                    subtitle = "Tap '+ Member' or 'Load Starter Gym Data' above to start tracking member check-ins."
                )
            }
        } else {
            items(members.take(5), key = { it.id }) { member ->
                ExpressCheckInRow(
                    member = member,
                    onCheckIn = { onCheckInMember(member) }
                )
            }
        }
    }
}

@Composable
fun MembersTabContent(
    members: List<Member>,
    onOpenAddMember: () -> Unit,
    onCheckInMember: (Member) -> Unit,
    onCycleMemberStatus: (Member) -> Unit,
    onDeleteMember: (Member) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    val statusFilters = listOf("All", "Active", "Expiring", "Frozen", "Overdue")

    val filteredMembers = members.filter { member ->
        val matchesStatus = selectedFilter == "All" || member.status.equals(selectedFilter, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
            member.fullName.contains(searchQuery, ignoreCase = true) ||
            member.planName.contains(searchQuery, ignoreCase = true) ||
            member.assignedTrainer.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesQuery
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("members_list")
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Athlete Directory",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${members.size} members • Tap status pill to cycle state",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onOpenAddMember,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricLime,
                        contentColor = ObsidianSlate
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_member_fab_button")
                ) {
                    Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Member", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, tier, or coach...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_search_input")
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                statusFilters.forEach { status ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricLime.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricLime
                        )
                    )
                }
            }
        }

        if (filteredMembers.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Matching Athletes",
                    subtitle = "Add a new gym member or adjust your search/status filters."
                )
            }
        } else {
            items(filteredMembers, key = { it.id }) { member ->
                MemberDetailCard(
                    member = member,
                    onCheckIn = { onCheckInMember(member) },
                    onCycleStatus = { onCycleMemberStatus(member) },
                    onDelete = { onDeleteMember(member) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.16f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accent
            )
        }
    }
}

@Composable
private fun MembershipTierAnalyticsCard(members: List<Member>) {
    val tiers = listOf("Elite VIP", "Pro Athlete", "Starter", "Day Pass")
    val tierColors = listOf(ElectricLime, CyberCyan, BlazeAmber, EmeraldSuccess)
    val total = members.size.coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Membership Tier Mix",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Distribution across active & expiring plans",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
            ) {
                var startX = 0f
                if (members.isEmpty()) {
                    drawRoundRect(
                        color = ElevatedSlate,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(9f, 9f)
                    )
                } else {
                    tiers.forEachIndexed { index, tier ->
                        val count = members.count { it.planName == tier }
                        val fraction = count.toFloat() / total.toFloat()
                        val segWidth = size.width * fraction
                        if (segWidth > 0f) {
                            drawRect(
                                color = tierColors[index],
                                topLeft = Offset(startX, 0f),
                                size = Size(segWidth, size.height)
                            )
                            startX += segWidth
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                tiers.forEachIndexed { index, tier ->
                    val count = members.count { it.planName == tier }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(tierColors[index])
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$tier ($count)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressCheckInRow(
    member: Member,
    onCheckIn: () -> Unit
) {
    Surface(
        color = CardSlate,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${member.planName} • ${member.attendanceCount} visits • Last: ${member.lastCheckInDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onCheckIn,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime.copy(alpha = 0.18f),
                    contentColor = ElectricLime
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Check In", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun MemberDetailCard(
    member: Member,
    onCheckIn: () -> Unit,
    onCycleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (member.status) {
        "Active" -> EmeraldSuccess
        "Expiring" -> BlazeAmber
        "Overdue" -> CrimsonPulse
        else -> CyberCyan
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .testTag("member_card_${member.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${member.planName} ($${member.monthlyFee}/mo) • Coach: ${member.assignedTrainer.ifBlank { "Unassigned" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.18f),
                    shape = CircleShape,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onCycleStatus)
                ) {
                    Text(
                        text = member.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = member.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (member.goal.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = ElectricLime,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Goal: ${member.goal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (member.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = member.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${member.attendanceCount} Check-Ins • Last: ${member.lastCheckInDate}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricLime
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onCheckIn,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("checkin_button_${member.id}")
                    ) {
                        Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check In")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete ${member.fullName}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
