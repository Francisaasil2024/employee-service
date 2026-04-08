import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './EditEmployeeForm.css';

function EditEmployeeForm({ employee, onSave, onCancel }) {
  const [formData, setFormData] = useState({
    name: employee.name,
    username: employee.username || '',
    email: employee.email || '',
    department: employee.department || '',
    role: { id: employee.role?.id || '' }
  });
  const [roles, setRoles] = useState([]);
  const [error, setError] = useState(null);
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

    if (!formData.role.id) {
      setError('Please select a role');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await onSave(formData);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update employee');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card edit-form shadow-sm h-100">
      <div className="card-header bg-warning text-dark">
        <h5 className="mb-0">✏️ Edit Employee</h5>
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

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="edit-name" className="form-label">
              Full Name <span className="text-danger">*</span>
            </label>
            <input
              type="text"
              className="form-control"
              id="edit-name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter employee name"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="edit-username" className="form-label">
              Username
            </label>
            <input
              type="text"
              className="form-control"
              id="edit-username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter username"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="edit-email" className="form-label">
              Email
            </label>
            <input
              type="email"
              className="form-control"
              id="edit-email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Enter email"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="edit-department" className="form-label">
              Department
            </label>
            <input
              type="text"
              className="form-control"
              id="edit-department"
              name="department"
              value={formData.department}
              onChange={handleChange}
              placeholder="Enter department"
              disabled={loading}
            />
          </div>

          <div className="mb-3">
            <label htmlFor="edit-roleId" className="form-label">
              Role <span className="text-danger">*</span>
            </label>
            <select
              className="form-select"
              id="edit-roleId"
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
          </div>

          <div className="d-grid gap-2">
            <button
              type="submit"
              className="btn btn-warning"
              disabled={loading}
            >
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onCancel}
              disabled={loading}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditEmployeeForm;
