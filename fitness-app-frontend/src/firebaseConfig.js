import { initializeApp } from "firebase/app";
import { getAuth, setPersistence, browserLocalPersistence } from "firebase/auth";
import { getAnalytics } from "firebase/analytics";

// Your exact Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyChFSaBYgzuOG3gILSUOlinrGiDSzEdrjU",
  authDomain: "fitnessmicroservice-f8902.firebaseapp.com",
  projectId: "fitnessmicroservice-f8902",
  storageBucket: "fitnessmicroservice-f8902.firebasestorage.app",
  messagingSenderId: "378228301246",
  appId: "1:378228301246:web:a99ead91042c2c87fcadef",
  measurementId: "G-JWV5NHPPWB"
};

// Initialize Firebase App
const app = initializeApp(firebaseConfig);

// Initialize Analytics
export const analytics = getAnalytics(app);

// Initialize and export Auth
export const auth = getAuth(app);

// CRITICAL FIX: Set persistence to Local so the user stays logged in 
// even after a page refresh or redirect.
setPersistence(auth, browserLocalPersistence)
  .catch((error) => {
    console.error("Firebase persistence error:", error);
  });
