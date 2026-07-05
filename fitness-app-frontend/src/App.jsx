import React, { useState, useEffect } from 'react';
// 1. Removed Keycloak AuthContext. Added useSelector to grab token from Redux.
import { useDispatch, useSelector } from 'react-redux';
import { setCredentials, logout } from './store/authSlice';
import { BrowserRouter as Router, Navigate, Route, Routes } from "react-router-dom"; 
import Button from '@mui/material/Button'; 
import { Box } from '@mui/material';
import ActivityForm from './components/ActivityForm';
import ActivityList from './components/ActivityList';
import ActivityDetail from './components/ActivityDetail';

// 2. NEW: Import Firebase Auth tools
import { auth } from './firebaseConfig';
import { signInWithPopup, GoogleAuthProvider, onAuthStateChanged } from 'firebase/auth';

const ActivitiesPage = () => {
 return ( 
  <Box component="section" sx={{  p: 2 , border: '1px dashed #ccc', borderRadius: '4px', boxShadow: 3, backgroundColor: '#f9f9f9' }}> 
   <ActivityForm onActivityAdded={() => window.location.reload()} />
   <ActivityList /> 
  </Box>
 );
}

function App() {
  const dispatch = useDispatch();
  // 3. Read the token directly from your Redux store instead of Keycloak
  const token = useSelector((state) => state.auth.token);
  const [authReady, setAuthReady] = useState(false);

  // 4. NEW: Firebase global auth listener
  // This automatically watches for logins/logouts and updates Redux for you
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      if (user) {
        // User is logged in, grab the fresh JWT token
        const freshToken = await user.getIdToken();
        // Update Redux (this passes the uid to your authSlice!)
        dispatch(setCredentials({ token: freshToken, user: user }));
      } else {
        // User is logged out, clear Redux
        dispatch(logout());
      }
      setAuthReady(true);
    });

    // Cleanup the listener when the app unmounts
    return () => unsubscribe();
  }, [dispatch]); 

  // 5. NEW: Firebase Login Function
  const handleLogin = async () => {
    const provider = new GoogleAuthProvider();
    try {
      // This pops up the Google login window. 
      // Once successful, the onAuthStateChanged useEffect above catches it automatically!
      await signInWithPopup(auth, provider);
    } catch (error) {
      console.error("Firebase Login failed:", error);
    }
  };

  // Prevent UI flashing while Firebase checks if you are already logged in
  if (!authReady) {
    return <div>Loading Auth...</div>;
  }

  return (
    <Router>
      { !token ? (
        <Button 
          variant="contained" 
          color="primary"
          onClick={handleLogin} // Pointed to our new Firebase function
        > 
          Login 
        </Button> 
      ) : (
        <Box component="main" sx={{  p: 2 , border: '1px solid #ccc', borderRadius: '4px', boxShadow: 3, backgroundColor: '#f9f9f9' }}>
          <Routes>
            <Route path="/activities" element={<ActivitiesPage />} />
            <Route path="/activities/:id" element={<ActivityDetail />} />
            <Route path="/" element={token ? <Navigate to="/activities" replace  /> : <div> Welcome! Please Login </div>} />
          </Routes> 
        </Box>   
      )}
    </Router>
  )
}

export default App;
