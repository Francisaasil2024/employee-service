import React from 'react';
import './Login.css';

function Login({ onLogin }) {
  const handleSubmit = (e) => {
    e.preventDefault();
    onLogin();
  };

  return (
    <div className="login-container d-flex justify-content-center align-items-center min-vh-100">
      <div className="card login-form shadow-lg">
        <div className="card-header bg-primary text-white text-center py-4">
          <h4 className="mb-0">👋 Welcome to Payroll System</h4>
        </div>
        <div className="card-body p-4">
          <p className="text-center">No username or password needed. Click continue to see employees.</p>
          <form onSubmit={handleSubmit}>
            <div className="d-grid">
              <button type="submit" className="btn btn-primary btn-lg">
                Continue
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default Login;