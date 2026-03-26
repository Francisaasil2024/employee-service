import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import Navbar from './components/Navbar';
import Login from './components/Login';
import EmployeeList from './components/EmployeeList';
import RoleList from './components/RoleList';

function App() {
  const handleLogout = () => {
    window.location.href = '/';
  };

  return (
    <Router>
      <div className="App">
        <Navbar onLogout={handleLogout} />
        <div className="container mt-4">
          <Routes>
            <Route path="/login" element={<Login onLogin={() => window.location.href = '/employees'} />} />
            <Route path="/" element={<EmployeeList />} />
            <Route path="/employees" element={<EmployeeList />} />
            <Route path="/roles" element={<RoleList />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
