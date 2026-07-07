import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setCredentials, logout } from './store/authSlice';
import { BrowserRouter as Router, Navigate, Route, Routes } from "react-router-dom"; 
import Button from '@mui/material/Button'; 
import { Box } from '@mui/material';
import ActivityForm from './components/ActivityForm';
import ActivityList from './components/ActivityList';
import ActivityDetail from './components/ActivityDetail';

// 1. CLEANED FIREBASE IMPORTS (Only import what we need, and only once)
import { auth } from './firebaseConfig';
import { signInWithPopup, GoogleAuthProvider, onAuthStateChanged } from 'firebase/auth';

const ActivitiesPage = () => {
 return ( 
  <Box component="section" sx={{ p: 2, border: '1px dashed #ccc', borderRadius: '4px', boxShadow: 3, backgroundColor: '#f9f9f9' }}> 
   <ActivityForm onActivityAdded={() => window.location.reload()} />
   <ActivityList /> 
  </Box>
 );
}

function App() {
  const dispatch = useDispatch();
  const token = useSelector((state) => state.auth.token);
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      if (user) {
        const freshToken = await user.getIdToken();
        
        const safeUserData = {
          uid: user.uid,
          email: user.email,
          displayName: user.displayName
        };

        dispatch(setCredentials({ token: freshToken, user: safeUserData }));
      } else {
        dispatch(logout());
      }
      setAuthReady(true); // Now we know the auth state
    });

    return () => unsubscribe();
  }, [dispatch]); 

  // 2. THE SINGLE, CORRECT LOGIN FUNCTION (using Popup)
  const handleLogin = async () => {
    const provider = new GoogleAuthProvider();
    try {
      await signInWithPopup(auth, provider);
    } catch (error) {
      console.error("Firebase Login failed:", error);
    }
  };

  // If we haven't finished checking Firebase, show a loading screen
  if (!authReady) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      { !token ? (
        // Only render the Login button if we are sure there is no token
        <Button 
          variant="contained" 
          color="primary"
          onClick={handleLogin} 
        > 
          Login 
        </Button> 
      ) : (
        <Box component="main" sx={{ p: 2, border: '1px solid #ccc', borderRadius: '4px', boxShadow: 3, backgroundColor: '#f9f9f9' }}>
          <Routes>
            <Route path="/activities" element={<ActivitiesPage />} />
            <Route path="/activities/:id" element={<ActivityDetail />} />
            {/* If logged in and at root, redirect to activities */}
            <Route path="/" element={<Navigate to="/activities" replace />} />
          </Routes> 
        </Box>   
      )}
    </Router>
  )
}

export default App;
