import React, { useEffect, useState } from 'react';
import axios from 'axios';

function MyProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('Please login to view your profile.');
        setLoading(false);
        return;
      }

      try {
        const response = await axios.get('/auth/profile', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        setProfile(response.data);
      } catch (err) {
        setError('Failed to load profile. Please login again.');
        console.error('Profile load error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  const role = localStorage.getItem('role');
  const username = localStorage.getItem('username');

  if (loading) {
    return (
      <div className="text-center mt-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-2">Loading profile...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="alert alert-danger mt-4" role="alert">
        {error}
      </div>
    );
  }

  if (!profile) {
    return null;
  }

  return (
    <div className="card shadow-sm mt-4">
      <div className="card-header bg-primary text-white">
        <h3 className="mb-0">My Profile</h3>
      </div>
      <div className="card-body">
        {role === 'USER' && (
          <div className="alert alert-info">
            Welcome, {username}! Here is your profile.
          </div>
        )}

        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Full Name</div>
          <div className="col-md-9">{profile.fullName || '—'}</div>
        </div>
        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Username</div>
          <div className="col-md-9">{profile.username}</div>
        </div>
        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Email</div>
          <div className="col-md-9">{profile.email || '—'}</div>
        </div>
        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Department</div>
          <div className="col-md-9">{profile.department || '—'}</div>
        </div>
        <div className="row mb-2">
          <div className="col-md-3 fw-semibold">Role</div>
          <div className="col-md-9">
            <span className="badge bg-secondary">{profile.role}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default MyProfile;
