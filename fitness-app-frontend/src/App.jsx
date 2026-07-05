import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { setCredentials, logout } from './store/authSlice';
import { BrowserRouter as Router, Navigate, Route, Routes } from "react-router-dom"; 
import Button from '@mui/material/Button'; 
import { Box } from '@mui/material';
import ActivityForm from './components/ActivityForm';
import ActivityList from './components/ActivityList';
import ActivityDetail from './components/ActivityDetail';

// 1. CLEANED IMPORTS: We only need signInWithRedirect now
import { auth } from './firebaseConfig';
import { signInWithRedirect, GoogleAuthProvider, onAuthStateChanged } from 'firebase/auth';

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
  const token = useSelector((state) => state.auth.token);
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      if (user) {
        const freshToken = await user.getIdToken();
        
        // 2. REDUX FIX: Extract only the safe data so JSON.stringify doesn't crash
        const safeUserData = {
          uid: user.uid,
          email: user.email,
          displayName: user.displayName
        };

        // Pass the safeUserData to Redux instead of the raw user object
        dispatch(setCredentials({ token: freshToken, user: safeUserData }));
      } else {
        dispatch(logout());
      }
      setAuthReady(true);
    });

    return () => unsubscribe();
  }, [dispatch]); 

  const handleLogin = async () => {
    const provider = new GoogleAuthProvider();
    try {
      // 3. INCOGNITO FIX: Trigger the redirect instead of the popup
      await signInWithRedirect(auth, provider);
    } catch (error) {
      console.error("Firebase Login failed:", error);
    }
  };

  if (!authReady) {
    return <div>Loading Auth...</div>;
  }

  return (
    <Router>
      { !token ? (
        <Button 
          variant="contained" 
          color="primary"
          onClick={handleLogin} 
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
