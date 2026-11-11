package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Member(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val planName: String = "Pro Athlete",
    val monthlyFee: Long = 129L,
    val status: String = "Active",
    val assignedTrainer: String = "",
    val goal: String = "",
    val attendanceCount: Long = 0L,
    val lastCheckInDate: String = "Never",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GymClass(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val coachName: String = "",
    val category: String = "Strength",
    val scheduleTime: String = "",
    val room: String = "",
    val capacity: Long = 16L,
    val enrolledCount: Long = 0L,
    val intensity: String = "High",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GymLead(
    @DocumentId val id: String = "",
    val userId: String = "",
    val prospectName: String = "",
    val phone: String = "",
    val source: String = "Walk-In",
    val interestedPlan: String = "Pro Athlete",
    val stage: String = "New",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GymPayment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val amount: Long = 129L,
    val planName: String = "Pro Athlete",
    val paymentMethod: String = "Card",
    val status: String = "Paid",
    val billingPeriod: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserAccount(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "member",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MemberProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val age: Long = 25L,
    val address: String = "",
    val planName: String = "Pro Athlete",
    val monthlyFee: Long = 129L,
    val photoUri: String = "",
    val role: String = "member",
    val status: String = "pending",
    val startDate: String = "Pending Approval",
    val expiryDate: String = "Pending Approval",
    val daysRemaining: Long = 0L,
    val paymentStatus: String = "Pending",
    val attendanceCount: Long = 0L,
    val recentActivity: String = "Registration submitted — awaiting admin approval",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MemberPayment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val memberName: String = "",
    val planName: String = "Pro Athlete",
    val amount: Long = 129L,
    val paymentMethod: String = "Card",
    val status: String = "Paid",
    val billingPeriod: String = "",
    val receiptNumber: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MemberAttendance(
    @DocumentId val id: String = "",
    val userId: String = "",
    val memberName: String = "",
    val checkInDate: String = "",
    val checkInTime: String = "",
    val sessionType: String = "Strength & Conditioning",
    val verifiedBy: String = "Admin Desk",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RenewalRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val memberName: String = "",
    val currentPlan: String = "Starter",
    val requestedPlan: String = "Pro Athlete",
    val durationMonths: Long = 3L,
    val paymentPreference: String = "Card",
    val notes: String = "",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GymAnnouncement(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "General",
    val priority: String = "Important",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
