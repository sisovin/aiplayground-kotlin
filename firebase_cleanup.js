
// Firebase cleanup script - run this in browser console
// Go to https://console.firebase.google.com/project/aiagents-playapp/database/aiagents-playapp-default-rtdb/data
// Open browser developer tools (F12) and paste this in the Console tab

// Remove schema and tables (they are not needed for Firebase)
firebase.database().ref("schema").remove().then(() => {
  console.log("Schema removed successfully");
}).catch((error) => {
  console.error("Error removing schema:", error);
});

firebase.database().ref("tables").remove().then(() => {
  console.log("Tables removed successfully");
}).catch((error) => {
  console.error("Error removing tables:", error);
});

// Verify only agents remain
firebase.database().ref().once("value").then((snapshot) => {
  console.log("Current database structure:", Object.keys(snapshot.val() || {}));
});

