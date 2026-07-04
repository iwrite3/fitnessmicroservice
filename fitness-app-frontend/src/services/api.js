import axios from 'axios';
import { auth } from '../firebaseConfig'; // Ensure this path points to your new Firebase file

// Update this URL to your Render API Gateway once deployed.
// For local testing, you can leave it as 'http://localhost:8080/api'
const API_URL = 'https://YOUR_RENDER_GATEWAY_URL.onrender.com/api';

const api = axios.create({
  baseURL: API_URL,
});

// The new Firebase Interceptor
api.interceptors.request.use(
  async (config) => {
    // 1. Check if a user is currently logged in via Firebase
    const user = auth.currentUser;

    if (user) {
      // 2. Get the fresh JWT token (Firebase handles refreshing it if expired)
      const token = await user.getIdToken();
      config.headers['Authorization'] = `Bearer ${token}`;

      // 3. Since your backend previously relied on 'X-User-ID', 
      // we can now safely pass the Firebase unique ID (uid) here!
      config.headers['X-User-ID'] = user.uid;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Your existing API calls remain completely untouched! 
// The components using these won't even know the auth system changed.
export const getActivities = () => api.get('/activities');
export const addActivity = (activity) => api.post('/activities', activity);
export const getActivityDetail = (id) => api.get(`/activities/${id}`);
export const getActivityRecommendation = (id) => api.get(`/recommendations/activity/${id}`);
