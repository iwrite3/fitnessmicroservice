import React from 'react'
import ReactDOM from 'react-dom/client'
import { Provider } from 'react-redux'
import { store } from './store/store'
import App from './App'

// Removed AuthProvider and authConfig imports completely.
// Firebase doesn't need a wrapper here because we handle it inside App.jsx!

const root = ReactDOM.createRoot(document.getElementById('root'))
root.render(
  <Provider store={store}>
    <App />
  </Provider>
)
