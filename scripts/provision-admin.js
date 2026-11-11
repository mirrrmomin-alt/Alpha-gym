/**
 * ALPHA GYM CENTRE CRM — Trusted Server-Side First-Admin Provisioning Script
 *
 * Usage:
 *   export GOOGLE_APPLICATION_CREDENTIALS="/path/to/serviceAccountKey.json"
 *   export ALPHA_ADMIN_INITIAL_PASSWORD="<your-secure-admin-password>"
 *   node scripts/provision-admin.js
 *
 * Security Architecture:
 * - Creates or updates the fixed owner administrator in Firebase Authentication (alphagym@yahoo.com).
 * - Writes the trusted server-side registry document in /admins/{uid} (never client-writable).
 * - Writes the verified admin profile in /users/{uid} with role = "admin" and status = "active".
 * - Public registration is strictly blocked from creating admin documents or using alphagym@yahoo.com.
 */

const admin = require("firebase-admin");

const OWNER_ADMIN_EMAIL = "alphagym@yahoo.com";
const OWNER_ADMIN_NAME = "ALPHA GYM ADMIN";
const OWNER_ADMIN_PHONE = "+1 (555) 010-9999";

async function provisionOwnerAdmin() {
  const adminPassword = process.env.ALPHA_ADMIN_INITIAL_PASSWORD;
  if (!adminPassword || adminPassword.length < 8) {
    console.error(
      "ERROR: Set ALPHA_ADMIN_INITIAL_PASSWORD environment variable before running this script."
    );
    process.exit(1);
  }

  if (!admin.apps.length) {
    admin.initializeApp({
      credential: admin.credential.applicationDefault(),
    });
  }

  const auth = admin.auth();
  const db = admin.firestore();

  let userRecord;
  try {
    userRecord = await auth.getUserByEmail(OWNER_ADMIN_EMAIL);
    userRecord = await auth.updateUser(userRecord.uid, {
      password: adminPassword,
      displayName: OWNER_ADMIN_NAME,
      emailVerified: true,
      disabled: false,
    });
    console.log(`Updated existing Firebase Auth admin user: ${userRecord.uid}`);
  } catch (err) {
    if (err.code === "auth/user-not-found") {
      userRecord = await auth.createUser({
        email: OWNER_ADMIN_EMAIL,
        password: adminPassword,
        displayName: OWNER_ADMIN_NAME,
        emailVerified: true,
        disabled: false,
      });
      console.log(`Created new Firebase Auth admin user: ${userRecord.uid}`);
    } else {
      throw err;
    }
  }

  const uid = userRecord.uid;
  const now = admin.firestore.FieldValue.serverTimestamp();

  // 1. Provision trusted server-side admin registry (/admins/{uid})
  await db.collection("admins").doc(uid).set(
    {
      userId: uid,
      email: OWNER_ADMIN_EMAIL,
      createdAt: now,
    },
    { merge: true }
  );

  // 2. Provision verified admin profile (/users/{uid})
  await db.collection("users").doc(uid).set(
    {
      name: OWNER_ADMIN_NAME,
      email: OWNER_ADMIN_EMAIL,
      phone: OWNER_ADMIN_PHONE,
      role: "admin",
      status: "active",
      createdAt: now,
      updatedAt: now,
    },
    { merge: true }
  );

  console.log("Successfully provisioned ALPHA GYM ADMIN in /admins/" + uid + " and /users/" + uid);
}

if (require.main === module) {
  provisionOwnerAdmin().catch((err) => {
    console.error("Failed to provision owner admin:", err);
    process.exit(1);
  });
}

module.exports = { provisionOwnerAdmin, OWNER_ADMIN_EMAIL, OWNER_ADMIN_NAME };
