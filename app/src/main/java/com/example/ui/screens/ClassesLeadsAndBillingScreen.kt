package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GymClass
import com.example.data.model.GymLead
import com.example.data.model.GymPayment
import com.example.ui.theme.BlazeAmber
import com.example.ui.theme.CardSlate
import com.example.ui.theme.CrimsonPulse
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianSlate

@Composable
fun ClassesTabContent(
    classes: List<GymClass>,
    onOpenAddClass: () -> Unit,
    onAdjustEnrollment: (GymClass, Long) -> Unit,
    onDeleteClass: (GymClass) -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    val categories = listOf("All", "Strength", "HIIT", "CrossFit", "Mobility", "Boxing")

    val filteredClasses = classes.filter {
        selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Class Schedule & Capacity",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${classes.size} sessions • Live athlete spot counter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onOpenAddClass,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = ObsidianSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Class", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }
            }
        }

        if (filteredClasses.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Classes Scheduled",
                    subtitle = "Create a group Strength, HIIT, or CrossFit session to start booking athletes."
                )
            }
        } else {
            items(filteredClasses, key = { it.id }) { gymClass ->
                GymClassCard(
                    gymClass = gymClass,
                    onIncrement = { onAdjustEnrollment(gymClass, 1L) },
                    onDecrement = { onAdjustEnrollment(gymClass, -1L) },
                    onDelete = { onDeleteClass(gymClass) }
                )
            }
        }
    }
}

@Composable
private fun GymClassCard(
    gymClass: GymClass,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit
) {
    val fillRatio = (gymClass.enrolledCount.toFloat() / gymClass.capacity.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val intensityColor = when (gymClass.intensity) {
        "Extreme" -> CrimsonPulse
        "High" -> BlazeAmber
        else -> EmeraldSuccess
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gymClass.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${gymClass.category} • ${gymClass.coachName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan
                    )
                }
                Surface(
                    color = intensityColor.copy(alpha = 0.18f),
                    shape = CircleShape
                ) {
                    Text(
                        text = gymClass.intensity,
                        style = MaterialTheme.typography.labelSmall,
                        color = intensityColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = gymClass.scheduleTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Room,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = gymClass.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Roster: ${gymClass.enrolledCount} / ${gymClass.capacity} athletes",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (fillRatio >= 1f) CrimsonPulse else ElectricLime
                )
                Text(
                    text = "${(fillRatio * 100).toInt()}% Full",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { fillRatio },
                color = if (fillRatio >= 1f) CrimsonPulse else ElectricLime,
                trackColor = ElevatedSlate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDecrement,
                        enabled = gymClass.enrolledCount > 0,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Remove spot", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("-1 Spot")
                    }
                    Button(
                        onClick = onIncrement,
                        enabled = gymClass.enrolledCount < gymClass.capacity,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricLime.copy(alpha = 0.2f),
                            contentColor = ElectricLime
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Book spot", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1 Spot")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete ${gymClass.title}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun LeadsTabContent(
    leads: List<GymLead>,
    onOpenAddLead: () -> Unit,
    onAdvanceLead: (GymLead) -> Unit,
    onConvertLead: (GymLead) -> Unit,
    onDeleteLead: (GymLead) -> Unit
) {
    var selectedStage by rememberSaveable { mutableStateOf("All") }
    val stages = listOf("All", "New", "Tour Booked", "Trial Active", "Won", "Lost")

    val filteredLeads = leads.filter {
        selectedStage == "All" || it.stage.equals(selectedStage, ignoreCase = true)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Sales Leads Pipeline",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${leads.size} prospects • 1-tap conversion to Member + Invoice",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onOpenAddLead,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlazeAmber,
                        contentColor = ObsidianSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Lead", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                stages.forEach { stage ->
                    FilterChip(
                        selected = selectedStage == stage,
                        onClick = { selectedStage = stage },
                        label = { Text(stage) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlazeAmber.copy(alpha = 0.2f),
                            selectedLabelColor = BlazeAmber
                        )
                    )
                }
            }
        }

        if (filteredLeads.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Prospects in Pipeline",
                    subtitle = "Log walk-ins, Instagram inquiries, and referrals to track tours and trials."
                )
            }
        } else {
            items(filteredLeads, key = { it.id }) { lead ->
                GymLeadCard(
                    lead = lead,
                    onAdvanceStage = { onAdvanceLead(lead) },
                    onConvert = { onConvertLead(lead) },
                    onDelete = { onDeleteLead(lead) }
                )
            }
        }
    }
}

@Composable
private fun GymLeadCard(
    lead: GymLead,
    onAdvanceStage: () -> Unit,
    onConvert: () -> Unit,
    onDelete: () -> Unit
) {
    val stageColor = when (lead.stage) {
        "Won" -> EmeraldSuccess
        "Trial Active" -> ElectricLime
        "Tour Booked" -> CyberCyan
        "Lost" -> CrimsonPulse
        else -> BlazeAmber
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.prospectName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${lead.source} • Interested in ${lead.interestedPlan} • ${lead.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan
                    )
                }
                Surface(
                    color = stageColor.copy(alpha = 0.18f),
                    shape = CircleShape
                ) {
                    Text(
                        text = lead.stage,
                        style = MaterialTheme.typography.labelSmall,
                        color = stageColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (lead.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lead.notes,
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAdvanceStage,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Next Stage")
                    }

                    if (lead.stage != "Won") {
                        Button(
                            onClick = onConvert,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricLime,
                                contentColor = ObsidianSlate
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Convert to Member", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete ${lead.prospectName}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun BillingTabContent(
    payments: List<GymPayment>,
    onOpenAddPayment: () -> Unit,
    onTogglePaymentStatus: (GymPayment) -> Unit,
    onDeletePayment: (GymPayment) -> Unit
) {
    val paidTotal = payments.filter { it.status == "Paid" }.sumOf { it.amount }
    val pendingTotal = payments.filter { it.status != "Paid" }.sumOf { it.amount }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Billing & Invoices",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Collected: $$paidTotal • Outstanding: $$pendingTotal",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSuccess
                    )
                }
                Button(
                    onClick = onOpenAddPayment,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = ObsidianSlate
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Invoice", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (payments.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Billing Transactions",
                    subtitle = "Create a membership invoice or convert a lead to automatically log payment records."
                )
            }
        } else {
            items(payments, key = { it.id }) { payment ->
                GymPaymentCard(
                    payment = payment,
                    onToggleStatus = { onTogglePaymentStatus(payment) },
                    onDelete = { onDeletePayment(payment) }
                )
            }
        }
    }
}

@Composable
private fun GymPaymentCard(
    payment: GymPayment,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val isPaid = payment.status == "Paid"
    val statusColor = when (payment.status) {
        "Paid" -> EmeraldSuccess
        "Overdue" -> CrimsonPulse
        else -> BlazeAmber
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
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
                    text = "${payment.memberName} • $${payment.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${payment.planName} • ${payment.paymentMethod} • ${payment.billingPeriod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = statusColor.copy(alpha = 0.18f),
                    shape = CircleShape,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onToggleStatus)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaid) "Paid" else "${payment.status} (Tap to Pay)",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete invoice",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
