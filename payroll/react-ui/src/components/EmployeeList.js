import React, { useState, useEffect } from 'react';
import axios from 'axios';
import EmployeeCard from './EmployeeCard';
import EmployeeForm from './EmployeeForm';
import './EmployeeList.css';

function EmployeeList() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    fetchEmployees();
  }, []);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await axios.get('http://localhost:8080/employees');
      let data = response.data;

      if (!Array.isArray(data)) {
        data = data ? [data] : [];
      }

      setEmployees(data);
    } catch (err) {
      setError('Failed to fetch employees. Please make sure the backend is running.');
      console.error('Error fetching employees:', err);
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this employee?')) {
      try {
        await axios.delete(`http://localhost:8080/employees/${id}`);
        setEmployees(prevEmployees =>
          Array.isArray(prevEmployees)
            ? prevEmployees.filter(emp => emp.id !== id)
            : []
        );
      } catch (err) {
        setError('Failed to delete employee');
        console.error('Error deleting employee:', err);
      }
    }
  };

  const handleUpdate = async (id, updatedEmployee) => {
    try {
      const response = await axios.put(`http://localhost:8080/employees/${id}`, updatedEmployee);
      const updatedData = response.data;
      setEmployees(prevEmployees =>
        Array.isArray(prevEmployees)
          ? prevEmployees.map(emp => emp.id === id ? updatedData : emp)
          : []
      );
    } catch (err) {
      setError('Failed to update employee');
      console.error('Error updating employee:', err);
    }
  };

  const handleAddEmployee = () => {
    setShowForm(true);
  };

  const handleFormClose = () => {
    setShowForm(false);
    fetchEmployees();
  };

  if (loading) {
    return (
      <div className="text-center mt-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-2">Loading employees...</p>
      </div>
    );
  }

  return (
    <div className="employee-list">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2>👥 Employee Management</h2>
          {!isAdmin && (
            <div className="text-muted">Welcome, {username}! Here is your profile.</div>
          )}
        </div>
        {isAdmin && (
          <button
            className="btn btn-success btn-lg"
            onClick={handleAddEmployee}
          >
            ➕ Add New Employee
          </button>
        )}
      </div>

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

      {showForm && (
        <div className="row mb-4">
          <div className="col-lg-6">
            <EmployeeForm onClose={handleFormClose} />
          </div>
        </div>
      )}

      <div className="row">
        {!Array.isArray(employees) || employees.length === 0 ? (
          <div className="col-12">
            <div className="alert alert-info text-center">
              <h5>No employees found</h5>
              <p>There are no employee records to display.</p>
            </div>
          </div>
        ) : (
          employees.map(employee => (
            <div key={employee.id} className="col-lg-4 col-md-6 mb-4">
              <EmployeeCard
                employee={employee}
                onDelete={handleDelete}
                onUpdate={handleUpdate}
                canEdit={isAdmin}
                canDelete={isAdmin}
              />
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default EmployeeList;