import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './EmployeeForm.css';

function EmployeeForm({ onClose }) {
  const [formData, setFormData] = useState({
    name: '',
    username: '',
    email: '',
    department: '',
    role: { id: '' }
  });
  const [roles, setRoles] = useState([]);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchRoles();
  }, []);

  const fetchRoles = async () => {
    try {
      const response = await axios.get('/roles');
      setRoles(response.data);
    } catch (err) {
      console.error('Error fetching roles:', err);
      setError('Failed to fetch roles');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    if (name === 'roleId') {
      setFormData(prev => ({
        ...prev,
        role: { id: parseInt(value, 10) }
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setError('Employee name is required');
      return;
    }

    if (!formData.username.trim()) {
      setError('Employee username is required');
      return;
    }

    if (!formData.role.id) {
      setError('Please select a role');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const token = localStorage.getItem('token');
      await axios.post('/employees', formData, {
        headers: {
          Authorization: token ? `Bearer ${token}` : ''
        }
      });

      setSuccess(true);
      setFormData({
        name: '',
        username: '',
        email: '',
        department: '',
        role: { id: '' }
      });

      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add employee');
      console.error('Error adding employee:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card employee-form shadow-sm">
      <div className="card-header bg-primary text-white">
        <h5 className="mb-0">➕ Add New Employee</h5>
      </div>
      <div className="card-body">
        {error && (
          <div className="alert alert-danger alert-dismissible fade show" role="alert">
            {error}
            <button
              type="button"
              className="btn-close"
              onClick={() => setError(null)}
            ></button>
          </div>
        )}

        {success && (
          <div className="alert alert-success alert-dismissible fade show" role="alert">
            ✅ Employee added successfully!
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="name" className="form-label">
              Full Name <span className="text-danger">*</span>
            </label>
            <input
              type="text"
              className="form-control"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter employee name"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="username" className="form-label">
              Username <span className="text-danger">*</span>
            </label>
            <input
              type="text"
              className="form-control"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter employee username"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="email" className="form-label">
              Email
            </label>
            <input
              type="email"
              className="form-control"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Enter employee email"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="department" className="form-label">
              Department
            </label>
            <input
              type="text"
              className="form-control"
              id="department"
              name="department"
              value={formData.department}
              onChange={handleChange}
              placeholder="Enter employee department"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="roleId" className="form-label">
              Role <span className="text-danger">*</span>
            </label>
            <select
              className="form-select"
              id="roleId"
              name="roleId"
              value={formData.role.id}
              onChange={handleChange}
              disabled={loading}
            >
              <option value="">-- Select a role --</option>
              {roles.map(role => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
            {roles.length === 0 && (
              <small className="text-muted">
                No roles available. Please add roles first.
              </small>
            )}
          </div>

          <div className="d-grid gap-2 d-sm-flex justify-content-sm-end">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Adding...' : 'Add Employee'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EmployeeForm;
