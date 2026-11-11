package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GymCrmRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createAndObserveMember_authenticatedOwner_emitsMemberAndChecksIn() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = GymCrmRepository(firestore)

    val memberId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createMember(
        fullName = "Marcus Vance",
        email = "marcus@ironpulse.fit",
        phone = "+1-555-0192",
        planName = "Elite VIP",
        monthlyFee = 199L,
        status = "Active",
        assignedTrainer = "Coach Elena",
        goal = "500lb Deadlift",
        notes = "Morning athlete"
      )
    }
    assertTrue(memberId.isNotEmpty())

    val emittedMembers = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeMembers(aliceUid).first { list -> list.any { it.id == memberId } }
    }
    val createdMember = emittedMembers.first { it.id == memberId }
    assertEquals("Marcus Vance", createdMember.fullName)
    assertEquals(0L, createdMember.attendanceCount)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.checkInMember(createdMember)
    }

    val updatedMember = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.getMemberById(aliceUid, memberId)
    }
    assertNotNull(updatedMember)
    assertEquals(1L, updatedMember?.attendanceCount)
  }

  @Test
  fun createClassLeadAndPayment_authenticatedOwner_succeeds() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = GymCrmRepository(firestore)

    val classId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createClass(
        title = "Barbell Club",
        coachName = "Coach Elena",
        category = "Strength",
        scheduleTime = "Mon/Wed/Fri 07:00 AM",
        room = "Platform A",
        capacity = 12L,
        intensity = "Extreme"
      )
    }
    assertTrue(classId.isNotEmpty())

    val leadId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createLead(
        prospectName = "Sophia Chen",
        phone = "+1-555-0148",
        source = "Instagram",
        interestedPlan = "Pro Athlete",
        stage = "Tour Booked",
        notes = "Thursday 6PM"
      )
    }
    assertTrue(leadId.isNotEmpty())

    val paymentId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createPayment(
        memberId = "mem_101",
        memberName = "Marcus Vance",
        amount = 199L,
        planName = "Elite VIP",
        paymentMethod = "Auto-Pay",
        status = "Paid",
        billingPeriod = "Nov 2026"
      )
    }
    assertTrue(paymentId.isNotEmpty())

    val classes = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeClasses(aliceUid).first { it.any { c -> c.id == classId } }
    }
    assertTrue(classes.any { it.id == classId })
  }

  @Test
  fun memberRegistrationAndRbac_memberCreatesPendingAccountAndProfileAndCannotSelfApproveOrReadOtherMember() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = GymCrmRepository(firestore)

    val profileId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.registerMemberProfile(
        fullName = "Rohan Sharma",
        email = ALICE_EMAIL,
        phone = "+1-555-9080",
        age = 27L,
        address = "240 Silver Ave",
        planName = "Pro Athlete",
        monthlyFee = 129L,
        photoUri = ""
      )
    }
    assertEquals(aliceUid, profileId)

    val aliceUserAccount = withTimeout(FLOW_TIMEOUT_MS) {
      aliceRepo.observeUserAccount(aliceUid).first { it != null }
    }
    assertNotNull(aliceUserAccount)
    assertEquals("member", aliceUserAccount?.role)
    assertEquals("pending", aliceUserAccount?.status)

    val aliceProfile = withTimeout(FLOW_TIMEOUT_MS) {
      aliceRepo.observeMyMemberProfile(aliceUid).first { it != null }
    }
    assertNotNull(aliceProfile)
    assertEquals("member", aliceProfile?.role)
    assertEquals("pending", aliceProfile?.status)

    // Member updates own contact info -> succeeds and syncs /users/{uid} and /member_profiles/{uid}
    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.updateMemberOwnContactInfo(
        memberUid = aliceUid,
        fullName = "Rohan Sharma Updated",
        phone = "+1-555-9999",
        age = 28L,
        address = "500 Alpha Blvd",
        photoUri = ""
      )
    }

    // Member submits renewal request -> succeeds
    val renewalId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.submitRenewalRequest(
        memberName = "Rohan Sharma Updated",
        currentPlan = "Pro Athlete",
        requestedPlan = "Elite VIP",
        durationMonths = 6L,
        paymentPreference = "Card",
        notes = "Upgrade to VIP"
      )
    }
    assertTrue(renewalId.isNotEmpty())

    // Non-admin member attempts to self-approve -> fails with PERMISSION_DENIED
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        aliceRepo.approveMemberRegistration(
          profile = aliceProfile!!,
          assignedPlan = "Elite VIP",
          monthlyFee = 199L,
          startDate = "2026-11-10",
          expiryDate = "2026-12-10",
          daysRemaining = 30L
        )
      }
      fail("Expected PERMISSION_DENIED when non-admin member tries to self-approve")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }

    // Bob signs in and tries to read Alice's member_profile or user account -> fails with PERMISSION_DENIED
    signInTestUser(BOB_EMAIL)
    val bobRepo = GymCrmRepository(firestore)
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        bobRepo.getMemberProfileById(aliceUid)
      }
      fail("Expected PERMISSION_DENIED when Bob reads Alice's member profile")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun crossUserAccess_bobCannotReadAliceMember_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = GymCrmRepository(firestore)
    val memberId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createMember(
        fullName = "Private Member",
        email = "private@ironpulse.fit",
        phone = "+1-555-0100",
        planName = "Starter",
        monthlyFee = 79L,
        assignedTrainer = "Coach",
        goal = "Fitness",
        notes = ""
      )
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = GymCrmRepository(firestore)
    try {
      withTimeout(DEFAULT_TIMEOUT_MS) {
        bobRepo.getMemberById(aliceUid, memberId)
      }
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED for cross-user read")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun observeMembers_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    auth.signOut()
    val repository = GymCrmRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeMembers(aliceUid).first()
      }
      fail("Expected exception when observing while signed out")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@ironpulse.test"
    const val BOB_EMAIL = "bob@ironpulse.test"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
