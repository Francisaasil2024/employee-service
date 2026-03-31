import React, { useState } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';
import Navbar from './Navbar';
import EmployeeList from './EmployeeList';
import RoleList from './RoleList';

function Dashboard() {
  const navigate = useNavigate();
  const [isHovering, setIsHovering] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('isLoggedIn');
    navigate('/login');
  };

  const logoutButtonStyle = {
    backgroundColor: isHovering ? '#dc2626' : '#ef4444',
    color: 'white',
    padding: '8px 16px',
    borderRadius: '6px',
    border: 'none',
    cursor: 'pointer',
    transition: 'background-color 0.3s'
  };

  const headerStyle = {
    backgroundColor: '#4f46e5',
    color: 'white',
    padding: '16px 24px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)'
  };

  const titleStyle = {
    margin: 0,
    fontSize: '1.5rem',
    fontWeight: '600'
  };

  return (
    <div className="App">
      <div style={headerStyle}>
        <h1 style={titleStyle}>Employee Dashboard</h1>
        <button
          style={logoutButtonStyle}
          onClick={handleLogout}
          onMouseEnter={() => setIsHovering(true)}
          onMouseLeave={() => setIsHovering(false)}
        >
          Logout
        </button>
      </div>
      <Navbar />
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