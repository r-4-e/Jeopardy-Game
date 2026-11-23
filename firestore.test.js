const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserProgress(uid, now) {
  return {
    userId: uid,
    username: "AlicePlayer",
    totalScore: 0,
    correctCount: 0,
    missedCount: 0,
    currentStreak: 0,
    bestStreak: 0,
    unlockedGeo: 10,
    unlockedMaths: 10,
    unlockedScience: 10,
    unlockedLit: 10,
    answeredIds: [],
    createdAt: now,
    updatedAt: now,
  };
}

function validLeaderboardEntry(uid, now) {
  return {
    userId: uid,
    username: "AlicePlayer",
    totalScore: 0,
    correctCount: 0,
    missedCount: 0,
    bestStreak: 0,
    highestTier: 10,
    visibility: "PUBLIC",
    createdAt: now,
    updatedAt: now,
  };
}

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
  }
});

test("Unauthenticated user cannot read or write users or leaderboard", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validUserProgress(ALICE_UID, past))
  );
  await assertFails(
    unauthDb.collection("leaderboard").where("visibility", "==", "PUBLIC").get()
  );
  await assertFails(
    unauthDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardEntry(ALICE_UID, past))
  );
});

test("Authenticated owner can create, read, update, and reset their UserProgress", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // First-time doc existence check returns cleanly
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Create initial profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validUserProgress(ALICE_UID, created))
  );

  // Action 1: Rename username
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      username: "AliceQuiz",
      updatedAt: new Date(Date.now() - 5000),
    })
  );

  // Action 2: Record answer & unlock next tier
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalScore: 10,
      correctCount: 1,
      missedCount: 0,
      currentStreak: 1,
      bestStreak: 1,
      unlockedGeo: 30,
      unlockedMaths: 10,
      unlockedScience: 10,
      unlockedLit: 10,
      answeredIds: ["geo_10_1"],
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Action 3: Reset board cycle
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      answeredIds: [],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field (@DocumentId id or extra key)
  await assertFails(
    aliceDb.collection("users").doc("extra_doc").set({
      id: "extra_doc",
      ...validUserProgress(ALICE_UID, created),
    })
  );

  // Reject invalid tier value (e.g., 25)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      unlockedGeo: 25,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      username: "FutureAlice",
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Cross-user isolation on users collection", async () => {
  const past = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(BOB_UID).set(validUserProgress(BOB_UID, past));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      totalScore: 9999,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set(validUserProgress(ALICE_UID, past))
  );
});

test("Leaderboard allows authenticated PUBLIC query and owner-only writes", async () => {
  const past = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("leaderboard").doc(BOB_UID).set({
      ...validLeaderboardEntry(BOB_UID, past),
      username: "BobFriend",
      totalScore: 140,
      highestTier: 50,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Create Alice's own leaderboard entry
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardEntry(ALICE_UID, past))
  );

  // Update Alice's own leaderboard stats
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      totalScore: 40,
      correctCount: 2,
      missedCount: 0,
      bestStreak: 2,
      highestTier: 30,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Rename Alice's leaderboard username
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      username: "AliceRenamed",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Matches Kotlin query: db.collection("leaderboard").whereEqualTo("visibility", "PUBLIC").limit(100)
  await assertSucceeds(
    aliceDb.collection("leaderboard").where("visibility", "==", "PUBLIC").limit(100).get()
  );

  // Unfiltered leaderboard query fails (rules are not filters)
  await assertFails(aliceDb.collection("leaderboard").get());

  // Alice cannot modify Bob's leaderboard entry
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).update({
      totalScore: 0,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Alice cannot create a spoofed leaderboard entry for Bob
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).set(validLeaderboardEntry(ALICE_UID, past))
  );
});
