import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './Navbar';
import EmployeeList from './EmployeeList';
import RoleList from './RoleList';

function Dashboard() {
  const handleLogout = () => {
    localStorage.removeItem('isLoggedIn');
    window.location.href = '/login';
  };

  return (
    <div className="App">
      <Navbar onLogout={handleLogout} />
      <div className="container mt-4">
        <Routes>
          <Route path="/" element={<EmployeeList />} />
          <Route path="/employees" element={<EmployeeList />} />
          <Route path="/roles" element={<RoleList />} />
        </Routes>
      </div>
    </div>
  );
}

export default Dashboard;