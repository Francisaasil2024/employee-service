import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './Navbar';
import EmployeeList from './EmployeeList';
import RoleList from './RoleList';

function Dashboard() {
  const headerStyle = {
    backgroundColor: '#4f46e5',
    color: 'white',
    padding: '16px 24px',
    display: 'flex',
    justifyContent: 'center',
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