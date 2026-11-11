package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GymAnnouncement
import com.example.data.model.GymClass
import com.example.data.model.GymLead
import com.example.data.model.GymPayment
import com.example.data.model.Member
import com.example.data.model.MemberAttendance
import com.example.data.model.MemberPayment
import com.example.data.model.MemberProfile
import com.example.data.model.RenewalRequest
import com.example.data.repository.GymCrmRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class GymCrmViewModel(
    private val repository: GymCrmRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    // --- ADMIN STREAMS ---

    val membersState: StateFlow<UiState<List<Member>>> = repository.observeMembers(currentUserId)
        .map<List<Member>, UiState<List<Member>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing members", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load gym members"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val classesState: StateFlow<UiState<List<GymClass>>> = repository.observeClasses(currentUserId)
        .map<List<GymClass>, UiState<List<GymClass>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing classes", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load class schedule"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val leadsState: StateFlow<UiState<List<GymLead>>> = repository.observeLeads(currentUserId)
        .map<List<GymLead>, UiState<List<GymLead>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing leads", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load sales leads"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val paymentsState: StateFlow<UiState<List<GymPayment>>> = repository.observePayments(currentUserId)
        .map<List<GymPayment>, UiState<List<GymPayment>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing payments", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load billing records"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val allMemberProfilesState: StateFlow<UiState<List<MemberProfile>>> = repository.observeAllMemberProfilesForAdmin()
        .map<List<MemberProfile>, UiState<List<MemberProfile>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing member profiles for admin", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load member registrations"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val allRenewalRequestsState: StateFlow<UiState<List<RenewalRequest>>> = repository.observeAllRenewalRequestsForAdmin()
        .map<List<RenewalRequest>, UiState<List<RenewalRequest>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing renewal requests for admin", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load renewal requests"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // --- MEMBER PORTAL STREAMS ---

    val myMemberProfileState: StateFlow<UiState<MemberProfile?>> = repository.observeMyMemberProfile(currentUserId)
        .map<MemberProfile?, UiState<MemberProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing own member profile", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load member profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val myMemberPaymentsState: StateFlow<UiState<List<MemberPayment>>> = repository.observeMemberPayments(currentUserId)
        .map<List<MemberPayment>, UiState<List<MemberPayment>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing member payments", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load payment receipts"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val myMemberAttendanceState: StateFlow<UiState<List<MemberAttendance>>> = repository.observeMemberAttendance(currentUserId)
        .map<List<MemberAttendance>, UiState<List<MemberAttendance>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing member attendance", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load attendance history"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val myRenewalRequestsState: StateFlow<UiState<List<RenewalRequest>>> = repository.observeMyRenewalRequests(currentUserId)
        .map<List<RenewalRequest>, UiState<List<RenewalRequest>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing renewal requests", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load renewal requests"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val announcementsState: StateFlow<UiState<List<GymAnnouncement>>> = repository.observeAnnouncements()
        .map<List<GymAnnouncement>, UiState<List<GymAnnouncement>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing announcements", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to load gym announcements"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    fun clearBanners() {
        _actionError.value = null
        _actionMessage.value = null
    }

    fun seedDemoGymData() {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.seedStarterGymData()
                _actionMessage.value = "Sample Alpha Gym roster, classes, leads, and invoices loaded!"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to seed sample data"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun addMember(
        fullName: String,
        email: String,
        phone: String,
        planName: String,
        monthlyFee: Long,
        status: String,
        assignedTrainer: String,
        goal: String,
        notes: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.createMember(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    planName = planName,
                    monthlyFee = monthlyFee,
                    status = status,
                    assignedTrainer = assignedTrainer,
                    goal = goal,
                    notes = notes
                )
                _actionMessage.value = "Member ${fullName.trim()} added to roster."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Could not add member"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun checkInMember(member: Member) {
        viewModelScope.launch {
            try {
                repository.checkInMember(member)
                _actionMessage.value = "${member.fullName} checked in (#${member.attendanceCount + 1})"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to record check-in"
            }
        }
    }

    fun cycleMemberStatus(member: Member) {
        val nextStatus = when (member.status) {
            "Active" -> "Expiring"
            "Expiring" -> "Frozen"
            "Frozen" -> "Overdue"
            else -> "Active"
        }
        viewModelScope.launch {
            try {
                repository.updateMemberStatus(member, nextStatus)
                _actionMessage.value = "${member.fullName} status updated to $nextStatus"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to update status"
            }
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            try {
                repository.deleteMember(member.id)
                _actionMessage.value = "${member.fullName} removed."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to delete member"
            }
        }
    }

    fun addClass(
        title: String,
        coachName: String,
        category: String,
        scheduleTime: String,
        room: String,
        capacity: Long,
        intensity: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.createClass(
                    title = title,
                    coachName = coachName,
                    category = category,
                    scheduleTime = scheduleTime,
                    room = room,
                    capacity = capacity,
                    intensity = intensity
                )
                _actionMessage.value = "Class '${title.trim()}' scheduled."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to schedule class"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun adjustClassEnrollment(gymClass: GymClass, delta: Long) {
        viewModelScope.launch {
            try {
                repository.adjustClassEnrollment(gymClass, delta)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Could not update class roster"
            }
        }
    }

    fun deleteClass(gymClass: GymClass) {
        viewModelScope.launch {
            try {
                repository.deleteClass(gymClass.id)
                _actionMessage.value = "Class '${gymClass.title}' removed."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to delete class"
            }
        }
    }

    fun addLead(
        prospectName: String,
        phone: String,
        source: String,
        interestedPlan: String,
        stage: String,
        notes: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.createLead(
                    prospectName = prospectName,
                    phone = phone,
                    source = source,
                    interestedPlan = interestedPlan,
                    stage = stage,
                    notes = notes
                )
                _actionMessage.value = "Lead ${prospectName.trim()} added to pipeline."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to add lead"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun advanceLeadStage(lead: GymLead) {
        val nextStage = when (lead.stage) {
            "New" -> "Tour Booked"
            "Tour Booked" -> "Trial Active"
            "Trial Active" -> "Won"
            "Won" -> "New"
            else -> "New"
        }
        viewModelScope.launch {
            try {
                repository.updateLeadStage(lead, nextStage)
                _actionMessage.value = "${lead.prospectName} moved to $nextStage"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to update lead stage"
            }
        }
    }

    fun convertLeadToMember(lead: GymLead) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.convertLeadToMember(lead)
                _actionMessage.value = "${lead.prospectName} converted to active member & billed!"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to convert lead"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun deleteLead(lead: GymLead) {
        viewModelScope.launch {
            try {
                repository.deleteLead(lead.id)
                _actionMessage.value = "Lead ${lead.prospectName} deleted."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to delete lead"
            }
        }
    }

    fun addPayment(
        memberId: String,
        memberName: String,
        amount: Long,
        planName: String,
        paymentMethod: String,
        status: String,
        billingPeriod: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.createPayment(
                    memberId = memberId,
                    memberName = memberName,
                    amount = amount,
                    planName = planName,
                    paymentMethod = paymentMethod,
                    status = status,
                    billingPeriod = billingPeriod
                )
                _actionMessage.value = "Invoice for $memberName ($$amount) logged."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to create invoice"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun markPaymentPaid(payment: GymPayment) {
        val nextStatus = if (payment.status == "Paid") "Pending" else "Paid"
        viewModelScope.launch {
            try {
                repository.updatePaymentStatus(payment, nextStatus)
                _actionMessage.value = "Invoice for ${payment.memberName} marked $nextStatus"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to update payment"
            }
        }
    }

    fun deletePayment(payment: GymPayment) {
        viewModelScope.launch {
            try {
                repository.deletePayment(payment.id)
                _actionMessage.value = "Payment record removed."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to delete payment"
            }
        }
    }

    // --- MEMBER PORTAL & ADMIN APPROVAL ACTIONS ---

    fun registerMember(
        fullName: String,
        email: String,
        phone: String,
        age: Long,
        address: String,
        planName: String,
        monthlyFee: Long,
        photoUri: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.registerMemberProfile(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    age = age,
                    address = address,
                    planName = planName,
                    monthlyFee = monthlyFee,
                    photoUri = photoUri
                )
                _actionMessage.value = "Registration submitted! Status: Pending Admin Approval."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to submit member registration"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun updateOwnProfile(
        fullName: String,
        phone: String,
        age: Long,
        address: String,
        photoUri: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.updateMemberOwnContactInfo(
                    memberUid = currentUserId,
                    fullName = fullName,
                    phone = phone,
                    age = age,
                    address = address,
                    photoUri = photoUri
                )
                _actionMessage.value = "Profile details updated."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to update profile"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun approveMemberRegistration(
        profile: MemberProfile,
        assignedPlan: String,
        monthlyFee: Long,
        startDate: String,
        expiryDate: String,
        daysRemaining: Long
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.approveMemberRegistration(
                    profile = profile,
                    assignedPlan = assignedPlan,
                    monthlyFee = monthlyFee,
                    startDate = startDate,
                    expiryDate = expiryDate,
                    daysRemaining = daysRemaining
                )
                _actionMessage.value = "${profile.fullName} approved for $assignedPlan! Member dashboard unlocked."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to approve member registration"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun rejectMemberRegistration(profile: MemberProfile) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.rejectMemberRegistration(profile)
                _actionMessage.value = "${profile.fullName} registration rejected."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to reject registration"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun suspendMemberRegistration(profile: MemberProfile) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.suspendMemberRegistration(profile)
                _actionMessage.value = "${profile.fullName} account suspended."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to suspend member"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun activateMemberRegistration(profile: MemberProfile) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.activateMemberRegistration(profile)
                _actionMessage.value = "${profile.fullName} account activated."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to activate member"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun submitRenewalRequest(
        memberName: String,
        currentPlan: String,
        requestedPlan: String,
        durationMonths: Long,
        paymentPreference: String,
        notes: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.submitRenewalRequest(
                    memberName = memberName,
                    currentPlan = currentPlan,
                    requestedPlan = requestedPlan,
                    durationMonths = durationMonths,
                    paymentPreference = paymentPreference,
                    notes = notes
                )
                _actionMessage.value = "Renewal request for $requestedPlan ($durationMonths mo) submitted to Admin!"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to submit renewal request"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun updateRenewalRequestStatus(request: RenewalRequest, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateRenewalRequestStatus(request, newStatus)
                _actionMessage.value = "Renewal request for ${request.memberName} marked $newStatus."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to update renewal request"
            }
        }
    }

    fun postAnnouncement(
        title: String,
        body: String,
        category: String,
        priority: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                repository.createAnnouncement(
                    title = title,
                    body = body,
                    category = category,
                    priority = priority
                )
                _actionMessage.value = "Announcement '$title' published to Member Portal!"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _actionError.value = e.localizedMessage ?: "Failed to publish announcement"
            } finally {
                _isBusy.value = false
            }
        }
    }

    private companion object {
        const val TAG = "GymCrmVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
