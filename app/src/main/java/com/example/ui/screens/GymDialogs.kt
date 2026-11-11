package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardSlate
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ObsidianSlate

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        fullName: String,
        email: String,
        phone: String,
        planName: String,
        monthlyFee: Long,
        status: String,
        assignedTrainer: String,
        goal: String,
        notes: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var planName by remember { mutableStateOf("Pro Athlete") }
    var monthlyFeeText by remember { mutableStateOf("129") }
    var assignedTrainer by remember { mutableStateOf("Coach Elena Rostova") }
    var goal by remember { mutableStateOf("Strength & Hypertrophy") }
    var notes by remember { mutableStateOf("") }

    val plans = listOf("Starter" to "79", "Pro Athlete" to "129", "Elite VIP" to "199", "Day Pass" to "25")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = { Text("Enroll New Gym Member") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_member_name_input")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Select Membership Tier")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    plans.forEach { (plan, defaultPrice) ->
                        FilterChip(
                            selected = planName == plan,
                            onClick = {
                                planName = plan
                                monthlyFeeText = defaultPrice
                            },
                            label = { Text("$plan ($$defaultPrice)") }
                        )
                    }
                }
                OutlinedTextField(
                    value = monthlyFeeText,
                    onValueChange = { monthlyFeeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly Fee (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = assignedTrainer,
                    onValueChange = { assignedTrainer = it },
                    label = { Text("Assigned Coach / Trainer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Primary Fitness Goal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("CRM Notes / Medical / Preferences") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = monthlyFeeText.toLongOrNull() ?: 129L
                    onConfirm(
                        fullName.ifBlank { "New Athlete" },
                        email.ifBlank { "athlete@ironpulse.fit" },
                        phone.ifBlank { "+1 (555) 010-9999" },
                        planName,
                        fee,
                        "Active",
                        assignedTrainer,
                        goal,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ObsidianSlate
                ),
                modifier = Modifier.testTag("dialog_member_confirm_btn")
            ) {
                Text("Save Member", fontWeight = FontWeight.Bold)
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
fun AddClassDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        coachName: String,
        category: String,
        scheduleTime: String,
        room: String,
        capacity: Long,
        intensity: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var coachName by remember { mutableStateOf("Coach Elena Rostova") }
    var category by remember { mutableStateOf("Strength") }
    var scheduleTime by remember { mutableStateOf("Mon / Wed / Fri • 06:00 PM") }
    var room by remember { mutableStateOf("Main Platform") }
    var capacityText by remember { mutableStateOf("16") }
    var intensity by remember { mutableStateOf("High") }

    val categories = listOf("Strength", "HIIT", "CrossFit", "Mobility", "Boxing")
    val intensities = listOf("Moderate", "High", "Extreme")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = { Text("Schedule Group Class") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Class Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = coachName,
                    onValueChange = { coachName = it },
                    label = { Text("Lead Coach *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = scheduleTime,
                    onValueChange = { scheduleTime = it },
                    label = { Text("Days & Time Slot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Studio Room / Zone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Max Athlete Capacity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Intensity")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    intensities.forEach { level ->
                        FilterChip(
                            selected = intensity == level,
                            onClick = { intensity = level },
                            label = { Text(level) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = (capacityText.toLongOrNull() ?: 16L).coerceIn(1L, 500L)
                    onConfirm(
                        title.ifBlank { "Power Conditioning" },
                        coachName.ifBlank { "Coach Elena" },
                        category,
                        scheduleTime.ifBlank { "Daily • 06:00 PM" },
                        room.ifBlank { "Turf Zone" },
                        cap,
                        intensity
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ObsidianSlate
                )
            ) {
                Text("Schedule Class", fontWeight = FontWeight.Bold)
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
fun AddLeadDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        prospectName: String,
        phone: String,
        source: String,
        interestedPlan: String,
        stage: String,
        notes: String
    ) -> Unit
) {
    var prospectName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("Instagram") }
    var interestedPlan by remember { mutableStateOf("Elite VIP") }
    var notes by remember { mutableStateOf("") }

    val sources = listOf("Walk-In", "Instagram", "Referral", "Website")
    val plans = listOf("Starter", "Pro Athlete", "Elite VIP", "Day Pass")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = { Text("Add Sales Prospect Lead") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = prospectName,
                    onValueChange = { prospectName = it },
                    label = { Text("Prospect Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Acquisition Channel")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    sources.forEach { src ->
                        FilterChip(
                            selected = source == src,
                            onClick = { source = src },
                            label = { Text(src) }
                        )
                    }
                }
                Text("Target Membership Plan")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    plans.forEach { plan ->
                        FilterChip(
                            selected = interestedPlan == plan,
                            onClick = { interestedPlan = plan },
                            label = { Text(plan) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Tour Date") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        prospectName.ifBlank { "Walk-In Prospect" },
                        phone.ifBlank { "+1 (555) 019-2831" },
                        source,
                        interestedPlan,
                        "New",
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ObsidianSlate
                )
            ) {
                Text("Save Lead", fontWeight = FontWeight.Bold)
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
fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        memberName: String,
        amount: Long,
        planName: String,
        paymentMethod: String,
        status: String,
        billingPeriod: String
    ) -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("129") }
    var planName by remember { mutableStateOf("Pro Athlete") }
    var paymentMethod by remember { mutableStateOf("Card") }
    var status by remember { mutableStateOf("Paid") }
    var billingPeriod by remember { mutableStateOf("Nov 2026") }

    val methods = listOf("Card", "Auto-Pay", "Cash", "Bank Transfer")
    val statuses = listOf("Paid", "Pending", "Overdue")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSlate,
        title = { Text("Create Membership Invoice") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = memberName,
                    onValueChange = { memberName = it },
                    label = { Text("Member Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan / Package Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Amount (USD) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = billingPeriod,
                    onValueChange = { billingPeriod = it },
                    label = { Text("Billing Period (e.g. Nov 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Payment Method")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    methods.forEach { m ->
                        FilterChip(
                            selected = paymentMethod == m,
                            onClick = { paymentMethod = m },
                            label = { Text(m) }
                        )
                    }
                }
                Text("Settlement Status")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    statuses.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = (amountText.toLongOrNull() ?: 129L).coerceAtLeast(1L)
                    onConfirm(
                        memberName.ifBlank { "Studio Athlete" },
                        amt,
                        planName.ifBlank { "Pro Athlete" },
                        paymentMethod,
                        status,
                        billingPeriod.ifBlank { "Nov 2026" }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = ObsidianSlate
                )
            ) {
                Text("Log Invoice", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
