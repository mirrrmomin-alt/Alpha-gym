package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class GymCrmRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun currentTimeString(): String {
        return SimpleDateFormat("hh:mm a", Locale.US).format(Date())
    }

    private fun futureDateString(daysToAdd: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun currentBillingPeriod(): String {
        return SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
    }

    // --- ADMIN PORTAL: MEMBERS ---

    fun observeMembers(userId: String): Flow<List<Member>> {
        val path = "users/$userId/members"
        return db.collection("users").document(userId).collection("members")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Member::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun getMemberById(ownerUserId: String, memberId: String): Member? {
        val docRef = db.collection("users").document(ownerUserId).collection("members").document(memberId)
        return try {
            docRef.get().await().toObject(Member::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createMember(
        fullName: String,
        email: String,
        phone: String,
        planName: String,
        monthlyFee: Long,
        status: String = "Active",
        assignedTrainer: String,
        goal: String,
        notes: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("members").document()
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim().take(100),
            "email" to email.trim().take(120),
            "phone" to phone.trim().take(40),
            "planName" to planName,
            "monthlyFee" to monthlyFee.coerceIn(0L, 100000L),
            "status" to status,
            "assignedTrainer" to assignedTrainer.trim().take(100),
            "goal" to goal.trim().take(150),
            "attendanceCount" to 0L,
            "lastCheckInDate" to "Never",
            "notes" to notes.trim().take(500),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateMember(member: Member) {
        val uid = requireUserId()
        require(member.id.isNotBlank()) { "Member ID must not be blank" }
        requireNotNull(member.createdAt) { "Member createdAt must not be null" }
        val docRef = db.collection("users").document(uid).collection("members").document(member.id)
        try {
            docRef.set(member.copy(userId = uid, updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun checkInMember(member: Member) {
        val uid = requireUserId()
        require(member.id.isNotBlank()) { "Member ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("members").document(member.id)
        val updates = mapOf(
            "attendanceCount" to (member.attendanceCount + 1L).coerceAtMost(100000L),
            "lastCheckInDate" to todayDateString(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateMemberStatus(member: Member, newStatus: String) {
        val uid = requireUserId()
        require(member.id.isNotBlank()) { "Member ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("members").document(member.id)
        val updates = mapOf(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMember(memberId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("members").document(memberId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- ADMIN PORTAL: GYM CLASSES ---

    fun observeClasses(userId: String): Flow<List<GymClass>> {
        val path = "users/$userId/classes"
        return db.collection("users").document(userId).collection("classes")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GymClass::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createClass(
        title: String,
        coachName: String,
        category: String,
        scheduleTime: String,
        room: String,
        capacity: Long,
        intensity: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("classes").document()
        val safeCapacity = capacity.coerceIn(1L, 500L)
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(100),
            "coachName" to coachName.trim().take(100),
            "category" to category,
            "scheduleTime" to scheduleTime.trim().take(60),
            "room" to room.trim().take(60),
            "capacity" to safeCapacity,
            "enrolledCount" to 0L,
            "intensity" to intensity,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun adjustClassEnrollment(gymClass: GymClass, delta: Long) {
        val uid = requireUserId()
        require(gymClass.id.isNotBlank()) { "Class ID must not be blank" }
        val newEnrolled = (gymClass.enrolledCount + delta).coerceIn(0L, gymClass.capacity)
        val docRef = db.collection("users").document(uid).collection("classes").document(gymClass.id)
        val updates = mapOf(
            "enrolledCount" to newEnrolled,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteClass(classId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("classes").document(classId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- ADMIN PORTAL: LEADS (CRM PIPELINE) ---

    fun observeLeads(userId: String): Flow<List<GymLead>> {
        val path = "users/$userId/leads"
        return db.collection("users").document(userId).collection("leads")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GymLead::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createLead(
        prospectName: String,
        phone: String,
        source: String,
        interestedPlan: String,
        stage: String = "New",
        notes: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("leads").document()
        val payload = mapOf(
            "userId" to uid,
            "prospectName" to prospectName.trim().take(100),
            "phone" to phone.trim().take(40),
            "source" to source,
            "interestedPlan" to interestedPlan.trim().take(60),
            "stage" to stage,
            "notes" to notes.trim().take(400),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateLeadStage(lead: GymLead, nextStage: String) {
        val uid = requireUserId()
        require(lead.id.isNotBlank()) { "Lead ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("leads").document(lead.id)
        val updates = mapOf(
            "stage" to nextStage,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun convertLeadToMember(lead: GymLead): String {
        updateLeadStage(lead, "Won")
        val plan = if (lead.interestedPlan in listOf("Starter", "Pro Athlete", "Elite VIP", "Day Pass")) {
            lead.interestedPlan
        } else {
            "Pro Athlete"
        }
        val fee = when (plan) {
            "Starter" -> 79L
            "Pro Athlete" -> 129L
            "Elite VIP" -> 199L
            else -> 25L
        }
        val emailSlug = lead.prospectName.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), ".")
        val newMemberId = createMember(
            fullName = lead.prospectName,
            email = "$emailSlug@member.fit",
            phone = lead.phone,
            planName = plan,
            monthlyFee = fee,
            status = "Active",
            assignedTrainer = "Coach Marcus",
            goal = "Strength & Conditioning",
            notes = "Converted from ${lead.source} lead. ${lead.notes}".trim()
        )
        createPayment(
            memberId = newMemberId,
            memberName = lead.prospectName,
            amount = fee,
            planName = plan,
            paymentMethod = "Card",
            status = "Paid",
            billingPeriod = currentBillingPeriod()
        )
        return newMemberId
    }

    suspend fun deleteLead(leadId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("leads").document(leadId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- ADMIN PORTAL: PAYMENTS & BILLING ---

    fun observePayments(userId: String): Flow<List<GymPayment>> {
        val path = "users/$userId/payments"
        return db.collection("users").document(userId).collection("payments")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GymPayment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createPayment(
        memberId: String,
        memberName: String,
        amount: Long,
        planName: String,
        paymentMethod: String,
        status: String,
        billingPeriod: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("payments").document()
        val safeMemberId = memberId.ifBlank { "mem_general" }.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val payload = mapOf(
            "userId" to uid,
            "memberId" to safeMemberId.take(128),
            "memberName" to memberName.trim().take(100),
            "amount" to amount.coerceIn(1L, 1000000L),
            "planName" to planName.trim().take(60),
            "paymentMethod" to paymentMethod,
            "status" to status,
            "billingPeriod" to billingPeriod.trim().take(60),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updatePaymentStatus(payment: GymPayment, newStatus: String) {
        val uid = requireUserId()
        require(payment.id.isNotBlank()) { "Payment ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("payments").document(payment.id)
        val updates = mapOf(
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deletePayment(paymentId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("payments").document(paymentId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- /users/{userId} ROLE & STATUS VERIFICATION + MEMBER PORTAL & ADMIN APPROVALS ---

    fun observeUserAccount(userId: String): Flow<UserAccount?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun getUserAccount(userId: String): UserAccount? {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(UserAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun isTrustedServerAdmin(userId: String): Boolean {
        val adminRef = db.collection("admins").document(userId)
        val userRef = db.collection("users").document(userId)
        return try {
            val adminSnap = adminRef.get().await()
            if (!adminSnap.exists()) return false
            val userSnap = userRef.get().await()
            if (!userSnap.exists()) return true
            val role = userSnap.getString("role")
            val status = userSnap.getString("status")
            role == "admin" && status == "active"
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    fun observeMyMemberProfile(userId: String): Flow<MemberProfile?> {
        val path = "member_profiles/$userId"
        return db.collection("member_profiles").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(MemberProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
    }

    fun observeAllMemberProfilesForAdmin(): Flow<List<MemberProfile>> {
        val path = "member_profiles"
        return db.collection("member_profiles")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(MemberProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun getMemberProfileById(memberUid: String): MemberProfile? {
        val docRef = db.collection("member_profiles").document(memberUid)
        return try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(MemberProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun registerMemberProfile(
        fullName: String,
        email: String,
        phone: String,
        age: Long,
        address: String,
        planName: String,
        monthlyFee: Long,
        photoUri: String
    ): String {
        val uid = requireUserId()
        val cleanName = fullName.trim().ifBlank { "Alpha Gym Member" }.take(100)
        val cleanEmail = email.trim().ifBlank { "member@alphagym.com" }.take(120)
        require(!cleanEmail.equals("alphagym@yahoo.com", ignoreCase = true)) {
            "Public admin registration is strictly prohibited."
        }
        val cleanPhone = phone.trim().ifBlank { "+1 (555) 200-1000" }.take(40)
        val userDocRef = db.collection("users").document(uid)
        val docRef = db.collection("member_profiles").document(uid)
        val safePlan = if (planName in listOf("Starter", "Pro Athlete", "Elite VIP", "Day Pass")) {
            planName
        } else {
            "Pro Athlete"
        }
        val userPayload = mapOf(
            "name" to cleanName,
            "email" to cleanEmail,
            "phone" to cleanPhone,
            "role" to "member",
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val payload = mapOf(
            "userId" to uid,
            "fullName" to cleanName,
            "email" to cleanEmail,
            "phone" to cleanPhone,
            "age" to age.coerceIn(12L, 120L),
            "address" to address.trim().ifBlank { "Alpha Gym District" }.take(200),
            "planName" to safePlan,
            "monthlyFee" to monthlyFee.coerceIn(0L, 100000L),
            "photoUri" to photoUri.trim().take(500),
            "role" to "member",
            "status" to "pending",
            "startDate" to "Pending Approval",
            "expiryDate" to "Pending Approval",
            "daysRemaining" to 0L,
            "paymentStatus" to "Pending",
            "attendanceCount" to 0L,
            "recentActivity" to "Registration submitted — awaiting admin approval",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userDocRef.set(userPayload).await()
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateMemberOwnContactInfo(
        memberUid: String,
        fullName: String,
        phone: String,
        age: Long,
        address: String,
        photoUri: String
    ) {
        val uid = requireUserId()
        require(uid == memberUid) { "Members can only update their own profile." }
        val cleanName = fullName.trim().ifBlank { "Alpha Gym Member" }.take(100)
        val cleanPhone = phone.trim().ifBlank { "+1 (555) 200-1000" }.take(40)
        val docRef = db.collection("member_profiles").document(memberUid)
        val updates = mapOf(
            "fullName" to cleanName,
            "phone" to cleanPhone,
            "age" to age.coerceIn(12L, 120L),
            "address" to address.trim().ifBlank { "Alpha Gym District" }.take(200),
            "photoUri" to photoUri.trim().take(500),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            val userRef = db.collection("users").document(memberUid)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "name" to cleanName,
                        "phone" to cleanPhone,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun approveMemberRegistration(
        profile: MemberProfile,
        assignedPlan: String,
        monthlyFee: Long,
        startDate: String,
        expiryDate: String,
        daysRemaining: Long
    ) {
        requireUserId()
        val targetId = profile.id.ifBlank { profile.userId }
        require(targetId.isNotBlank()) { "Member profile ID must not be blank" }
        val safePlan = if (assignedPlan in listOf("Starter", "Pro Athlete", "Elite VIP", "Day Pass")) {
            assignedPlan
        } else {
            profile.planName
        }
        val docRef = db.collection("member_profiles").document(targetId)
        val updates = mapOf(
            "planName" to safePlan,
            "monthlyFee" to monthlyFee.coerceIn(0L, 100000L),
            "status" to "active",
            "startDate" to startDate.trim().ifBlank { todayDateString() }.take(40),
            "expiryDate" to expiryDate.trim().ifBlank { futureDateString(30) }.take(40),
            "daysRemaining" to daysRemaining.coerceIn(0L, 3650L),
            "paymentStatus" to "Paid",
            "attendanceCount" to (profile.attendanceCount.coerceAtLeast(1L)),
            "recentActivity" to "Membership approved by Admin • Plan: $safePlan",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            val userRef = db.collection("users").document(targetId)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "status" to "active",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            createMemberPaymentReceipt(
                memberUid = profile.userId.ifBlank { targetId },
                memberName = profile.fullName,
                planName = safePlan,
                amount = monthlyFee.coerceAtLeast(1L),
                paymentMethod = "Card",
                status = "Paid",
                billingPeriod = currentBillingPeriod()
            )
            recordMemberPortalAttendance(
                memberUid = profile.userId.ifBlank { targetId },
                memberName = profile.fullName,
                sessionType = "Membership Onboarding & Orientation",
                verifiedBy = "Alpha Gym Admin"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun rejectMemberRegistration(profile: MemberProfile) {
        requireUserId()
        val targetId = profile.id.ifBlank { profile.userId }
        require(targetId.isNotBlank()) { "Member profile ID must not be blank" }
        val docRef = db.collection("member_profiles").document(targetId)
        val updates = mapOf(
            "status" to "rejected",
            "paymentStatus" to "Cancelled",
            "recentActivity" to "Registration declined by Admin",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            val userRef = db.collection("users").document(targetId)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "status" to "rejected",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun suspendMemberRegistration(profile: MemberProfile) {
        requireUserId()
        val targetId = profile.id.ifBlank { profile.userId }
        require(targetId.isNotBlank()) { "Member profile ID must not be blank" }
        val docRef = db.collection("member_profiles").document(targetId)
        val updates = mapOf(
            "status" to "suspended",
            "paymentStatus" to "Suspended",
            "recentActivity" to "Account suspended by Admin",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            val userRef = db.collection("users").document(targetId)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "status" to "suspended",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun activateMemberRegistration(profile: MemberProfile) {
        requireUserId()
        val targetId = profile.id.ifBlank { profile.userId }
        require(targetId.isNotBlank()) { "Member profile ID must not be blank" }
        val docRef = db.collection("member_profiles").document(targetId)
        val updates = mapOf(
            "status" to "active",
            "paymentStatus" to "Paid",
            "recentActivity" to "Account activated by Admin",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
            val userRef = db.collection("users").document(targetId)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "status" to "active",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeMemberPayments(memberUid: String): Flow<List<MemberPayment>> {
        val path = "member_payments"
        return db.collection("member_payments")
            .whereEqualTo("userId", memberUid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(MemberPayment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == memberUid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createMemberPaymentReceipt(
        memberUid: String,
        memberName: String,
        planName: String,
        amount: Long,
        paymentMethod: String,
        status: String,
        billingPeriod: String
    ): String {
        requireUserId()
        val docRef = db.collection("member_payments").document()
        val receiptNo = "ALPHA-RCP-${System.currentTimeMillis().toString().takeLast(6)}"
        val payload = mapOf(
            "userId" to memberUid.trim().take(128),
            "memberName" to memberName.trim().ifBlank { "Alpha Member" }.take(100),
            "planName" to planName.trim().ifBlank { "Pro Athlete" }.take(60),
            "amount" to amount.coerceIn(1L, 1000000L),
            "paymentMethod" to if (paymentMethod in listOf("Card", "Auto-Pay", "Cash", "Bank Transfer", "UPI")) paymentMethod else "Card",
            "status" to if (status in listOf("Paid", "Pending", "Overdue")) status else "Paid",
            "billingPeriod" to billingPeriod.trim().ifBlank { currentBillingPeriod() }.take(60),
            "receiptNumber" to receiptNo,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeMemberAttendance(memberUid: String): Flow<List<MemberAttendance>> {
        val path = "member_attendance"
        return db.collection("member_attendance")
            .whereEqualTo("userId", memberUid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(MemberAttendance::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == memberUid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun recordMemberPortalAttendance(
        memberUid: String,
        memberName: String,
        sessionType: String,
        verifiedBy: String = "Alpha Gym Admin"
    ): String {
        requireUserId()
        val docRef = db.collection("member_attendance").document()
        val payload = mapOf(
            "userId" to memberUid.trim().take(128),
            "memberName" to memberName.trim().ifBlank { "Alpha Member" }.take(100),
            "checkInDate" to todayDateString(),
            "checkInTime" to currentTimeString(),
            "sessionType" to sessionType.trim().ifBlank { "Strength & Conditioning" }.take(80),
            "verifiedBy" to verifiedBy.trim().ifBlank { "Alpha Gym Admin" }.take(80),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeMyRenewalRequests(memberUid: String): Flow<List<RenewalRequest>> {
        val path = "renewal_requests"
        return db.collection("renewal_requests")
            .whereEqualTo("userId", memberUid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RenewalRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == memberUid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeAllRenewalRequestsForAdmin(): Flow<List<RenewalRequest>> {
        val path = "renewal_requests"
        return db.collection("renewal_requests")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RenewalRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun submitRenewalRequest(
        memberName: String,
        currentPlan: String,
        requestedPlan: String,
        durationMonths: Long,
        paymentPreference: String,
        notes: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("renewal_requests").document()
        val safeRequestedPlan = if (requestedPlan in listOf("Starter", "Pro Athlete", "Elite VIP", "Day Pass")) {
            requestedPlan
        } else {
            "Pro Athlete"
        }
        val payload = mapOf(
            "userId" to uid,
            "memberName" to memberName.trim().ifBlank { "Alpha Member" }.take(100),
            "currentPlan" to currentPlan.trim().ifBlank { "Starter" }.take(60),
            "requestedPlan" to safeRequestedPlan,
            "durationMonths" to durationMonths.coerceIn(1L, 36L),
            "paymentPreference" to paymentPreference.trim().ifBlank { "Card" }.take(60),
            "notes" to notes.trim().take(400),
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateRenewalRequestStatus(request: RenewalRequest, newStatus: String) {
        requireUserId()
        require(request.id.isNotBlank()) { "Renewal request ID must not be blank" }
        val docRef = db.collection("renewal_requests").document(request.id)
        val updates = mapOf(
            "status" to if (newStatus in listOf("pending", "approved", "rejected")) newStatus else "approved",
            "notes" to request.notes,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeAnnouncements(): Flow<List<GymAnnouncement>> {
        val path = "announcements"
        return db.collection("announcements")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(GymAnnouncement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser != null) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun createAnnouncement(
        title: String,
        body: String,
        category: String,
        priority: String
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("announcements").document()
        val payload = mapOf(
            "authorId" to uid,
            "title" to title.trim().ifBlank { "Alpha Gym Notice" }.take(120),
            "body" to body.trim().ifBlank { "New gym announcement posted." }.take(1000),
            "category" to category.trim().ifBlank { "General" }.take(60),
            "priority" to if (priority in listOf("Normal", "Important", "Urgent")) priority else "Important",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- STARTER SAMPLE DATA SEEDING (ON USER ACTION) ---

    suspend fun seedStarterGymData() {
        val m1 = createMember(
            fullName = "Marcus Vance",
            email = "marcus.vance@athlete.io",
            phone = "+1 (555) 234-8910",
            planName = "Elite VIP",
            monthlyFee = 199L,
            status = "Active",
            assignedTrainer = "Coach Elena Rostova",
            goal = "500lb Deadlift & Olympic Lifting",
            notes = "Prefers 6:30 AM rack sessions. Mobility warmup focus."
        )
        val m2 = createMember(
            fullName = "Aria Montgomery",
            email = "aria.m@studiofit.com",
            phone = "+1 (555) 876-5432",
            planName = "Pro Athlete",
            monthlyFee = 129L,
            status = "Active",
            assignedTrainer = "Coach Devon Brooks",
            goal = "Metabolic Conditioning & VO2 Max",
            notes = "Training for HYROX competition in December."
        )
        val m3 = createMember(
            fullName = "Liam O'Connor",
            email = "liam.oc@gmail.com",
            phone = "+1 (555) 391-0042",
            planName = "Starter",
            monthlyFee = 79L,
            status = "Expiring",
            assignedTrainer = "Coach Elena Rostova",
            goal = "Lean Muscle & Core Stability",
            notes = "Annual renewal coming up next week."
        )

        createClass(
            title = "Iron Barbell Club",
            coachName = "Coach Elena Rostova",
            category = "Strength",
            scheduleTime = "Mon / Wed / Fri • 06:30 AM",
            room = "Main Lifting Platform",
            capacity = 14L,
            intensity = "Extreme"
        )
        createClass(
            title = "HYROX Engine Builder",
            coachName = "Coach Devon Brooks",
            category = "HIIT",
            scheduleTime = "Tue / Thu • 05:30 PM",
            room = "Turf Sprint Arena",
            capacity = 20L,
            intensity = "High"
        )
        createClass(
            title = "Kinetic Mobility & Recovery",
            coachName = "Coach Maya Lin",
            category = "Mobility",
            scheduleTime = "Sat • 09:30 AM",
            room = "Recovery Loft",
            capacity = 18L,
            intensity = "Moderate"
        )

        createLead(
            prospectName = "Sophia Chen",
            phone = "+1 (555) 612-9081",
            source = "Instagram",
            interestedPlan = "Elite VIP",
            stage = "Tour Booked",
            notes = "Interested in 1-on-1 personal training + sauna access."
        )
        createLead(
            prospectName = "Jordan Alvarez",
            phone = "+1 (555) 741-2290",
            source = "Referral",
            interestedPlan = "Pro Athlete",
            stage = "Trial Active",
            notes = "Referred by Marcus Vance. On day 3 of 7-day pass."
        )

        val period = currentBillingPeriod()
        createPayment(
            memberId = m1,
            memberName = "Marcus Vance",
            amount = 199L,
            planName = "Elite VIP",
            paymentMethod = "Auto-Pay",
            status = "Paid",
            billingPeriod = period
        )
        createPayment(
            memberId = m2,
            memberName = "Aria Montgomery",
            amount = 129L,
            planName = "Pro Athlete",
            paymentMethod = "Card",
            status = "Paid",
            billingPeriod = period
        )
        createPayment(
            memberId = m3,
            memberName = "Liam O'Connor",
            amount = 79L,
            planName = "Starter",
            paymentMethod = "Bank Transfer",
            status = "Pending",
            billingPeriod = period
        )
    }
}
