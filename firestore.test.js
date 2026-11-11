const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_owner_alpha_gym_01";
const MEMBER_UID = "member_john_1";
const OTHER_MEMBER_UID = "member_sarah_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      const db = ctx.firestore();
      await db.collection("admins").doc(ADMIN_UID).set({
        userId: ADMIN_UID,
        email: "alphagym@yahoo.com",
        createdAt: new Date(Date.now() - 5000),
      });
      await db.collection("users").doc(ADMIN_UID).set({
        name: "ALPHA GYM ADMIN",
        email: "alphagym@yahoo.com",
        phone: "+1-555-010-9999",
        role: "admin",
        status: "active",
        createdAt: new Date(Date.now() - 5000),
        updatedAt: new Date(Date.now() - 5000),
      });
    });
  }
});

function validUserAccountPayload() {
  return {
    name: "John Carter",
    email: "john.carter@alphagym.com",
    phone: "+1-555-0199",
    role: "member",
    status: "pending",
    createdAt: new Date(Date.now() - 5000),
    updatedAt: new Date(Date.now() - 5000),
  };
}

function validMemberRegistrationPayload(uid) {
  return {
    userId: uid,
    fullName: "John Carter",
    email: "john.carter@alphagym.com",
    phone: "+1-555-0199",
    age: 28,
    address: "742 Evergreen Terrace, Suite 4B",
    planName: "Pro Athlete",
    monthlyFee: 129,
    photoUri: "",
    role: "member",
    status: "pending",
    startDate: "Pending Approval",
    expiryDate: "Pending Approval",
    daysRemaining: 0,
    paymentStatus: "Pending",
    attendanceCount: 0,
    recentActivity: "Registration submitted — awaiting admin approval",
    createdAt: new Date(Date.now() - 5000),
    updatedAt: new Date(Date.now() - 5000),
  };
}

test("1. Member registration creates pending /users/{uid} and /member_profiles/{uid}; cannot self-approve or become admin", async () => {
  const memberDb = testEnv.authenticatedContext(MEMBER_UID).firestore();
  const userRef = memberDb.collection("users").doc(MEMBER_UID);
  const profileRef = memberDb.collection("member_profiles").doc(MEMBER_UID);

  // Member cannot create /users/{uid} with role = admin or status = active
  await assertFails(
    userRef.set({ ...validUserAccountPayload(), role: "admin" })
  );
  await assertFails(
    userRef.set({ ...validUserAccountPayload(), status: "active" })
  );

  // Member cannot register /member_profiles/{uid} with role = admin or status = active
  await assertFails(
    profileRef.set({ ...validMemberRegistrationPayload(MEMBER_UID), role: "admin" })
  );
  await assertFails(
    profileRef.set({ ...validMemberRegistrationPayload(MEMBER_UID), status: "active" })
  );

  // Member cannot write to /admins
  await assertFails(
    memberDb.collection("admins").doc(MEMBER_UID).set({ userId: MEMBER_UID })
  );

  // Valid pending registration succeeds in both /users/{uid} and /member_profiles/{uid}
  await assertSucceeds(userRef.set(validUserAccountPayload()));
  await assertSucceeds(profileRef.set(validMemberRegistrationPayload(MEMBER_UID)));
});

test("2. Admin approves member (status = active), suspends member (status = suspended), and logs payment & attendance", async () => {
  const memberDb = testEnv.authenticatedContext(MEMBER_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();

  const userRef = memberDb.collection("users").doc(MEMBER_UID);
  const profileRef = memberDb.collection("member_profiles").doc(MEMBER_UID);
  await assertSucceeds(userRef.set(validUserAccountPayload()));
  await assertSucceeds(profileRef.set(validMemberRegistrationPayload(MEMBER_UID)));

  // Member cannot self-approve or escalate role in /users or /member_profiles
  await assertFails(
    userRef.update({ status: "active", updatedAt: new Date(Date.now() - 1000) })
  );
  await assertFails(
    userRef.update({ role: "admin", updatedAt: new Date(Date.now() - 1000) })
  );
  await assertFails(
    profileRef.update({ status: "active", updatedAt: new Date(Date.now() - 1000) })
  );

  // Admin can list pending registrations and approve member -> status = active
  await assertSucceeds(
    adminDb.collection("member_profiles").where("status", "==", "pending").get()
  );
  await assertSucceeds(
    adminDb.collection("users").doc(MEMBER_UID).update({
      status: "active",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    adminDb.collection("member_profiles").doc(MEMBER_UID).update({
      status: "active",
      planName: "Elite VIP",
      monthlyFee: 199,
      startDate: "2026-11-10",
      expiryDate: "2026-12-10",
      daysRemaining: 30,
      paymentStatus: "Paid",
      recentActivity: "Membership approved by Admin",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Admin can suspend and re-activate member
  await assertSucceeds(
    adminDb.collection("users").doc(MEMBER_UID).update({
      status: "suspended",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    adminDb.collection("member_profiles").doc(MEMBER_UID).update({
      status: "suspended",
      recentActivity: "Account suspended by Admin",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin logs payment receipt and attendance for member
  await assertSucceeds(
    adminDb.collection("member_payments").doc("pay_m1").set({
      userId: MEMBER_UID,
      memberName: "John Carter",
      planName: "Elite VIP",
      amount: 199,
      paymentMethod: "Card",
      status: "Paid",
      billingPeriod: "Nov 2026",
      receiptNumber: "AGC-2026-1001",
      createdAt: new Date(Date.now() - 2000),
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  await assertSucceeds(
    adminDb.collection("member_attendance").doc("att_m1").set({
      userId: MEMBER_UID,
      memberName: "John Carter",
      checkInDate: "2026-11-10",
      checkInTime: "07:30 AM",
      sessionType: "Strength & Conditioning",
      verifiedBy: "Alpha Desk Admin",
      createdAt: new Date(Date.now() - 2000),
      updatedAt: new Date(Date.now() - 2000),
    })
  );
});

test("3. Member can view own /users/{uid}, profile, payments, attendance, and submit renewal request, but cannot read other members or edit admin records", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(MEMBER_UID).set(validUserAccountPayload());
    await db.collection("users").doc(OTHER_MEMBER_UID).set({
      ...validUserAccountPayload(),
      email: "sarah@alphagym.com",
    });
    await db.collection("member_profiles").doc(MEMBER_UID).set(validMemberRegistrationPayload(MEMBER_UID));
    await db.collection("member_profiles").doc(OTHER_MEMBER_UID).set(validMemberRegistrationPayload(OTHER_MEMBER_UID));
    await db.collection("member_payments").doc("pay_1").set({
      userId: MEMBER_UID,
      memberName: "John Carter",
      planName: "Pro Athlete",
      amount: 129,
      paymentMethod: "Card",
      status: "Paid",
      billingPeriod: "Nov 2026",
      receiptNumber: "AGC-101",
      createdAt: new Date(Date.now() - 5000),
      updatedAt: new Date(Date.now() - 5000),
    });
  });

  const memberDb = testEnv.authenticatedContext(MEMBER_UID).firestore();

  // Member can read own /users/{uid}, profile, and payments
  await assertSucceeds(memberDb.collection("users").doc(MEMBER_UID).get());
  await assertSucceeds(memberDb.collection("member_profiles").doc(MEMBER_UID).get());
  await assertSucceeds(
    memberDb.collection("member_payments").where("userId", "==", MEMBER_UID).get()
  );

  // Member CANNOT read another member's /users/{uid} or /member_profiles/{uid} (PII isolation)
  await assertFails(memberDb.collection("users").doc(OTHER_MEMBER_UID).get());
  await assertFails(memberDb.collection("member_profiles").doc(OTHER_MEMBER_UID).get());

  // Member CANNOT modify payments or attendance
  await assertFails(
    memberDb.collection("member_payments").doc("pay_1").update({
      amount: 1,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Member CAN update their own name/phone in /users/{uid} and /member_profiles/{uid}
  await assertSucceeds(
    memberDb.collection("users").doc(MEMBER_UID).update({
      phone: "+1-555-9000",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    memberDb.collection("member_profiles").doc(MEMBER_UID).update({
      phone: "+1-555-9000",
      address: "900 Alpha Ave",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Member CAN submit a renewal request with status == pending
  await assertSucceeds(
    memberDb.collection("renewal_requests").doc("ren_1").set({
      userId: MEMBER_UID,
      memberName: "John Carter",
      currentPlan: "Pro Athlete",
      requestedPlan: "Elite VIP",
      durationMonths: 3,
      paymentPreference: "Card",
      notes: "Upgrading for personal training access",
      status: "pending",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
