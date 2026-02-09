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
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Get user data from localStorage
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (e) {
        console.error('Error parsing user data:', e);
      }
    }
    fetchEmployees();
  }, []);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await axios.get('/employees');
      
      // Safely handle the response - ensure it's always an array
      let data = response.data;
      
      // If response is wrapped in a structure, extract the array
      if (data && typeof data === 'object') {
        // If it's an array, use it directly
        if (Array.isArray(data)) {
          setEmployees(data);
        } 
        // If it's an object with an employees or results property, use that
        else if (Array.isArray(data.employees)) {
          setEmployees(data.employees);
        } 
        else if (Array.isArray(data.results)) {
          setEmployees(data.results);
        }
        // If it's an object with data property that's an array
        else if (Array.isArray(data.data)) {
          setEmployees(data.data);
        }
        // Otherwise, initialize as empty array
        else {
          console.warn('Unexpected response format:', data);
          setEmployees([]);
        }
      } else {
        // If response is not an object, set empty array
        setEmployees([]);
      }
    } catch (err) {
      setError('Failed to fetch employees. Make sure the backend is running on http://localhost:8080');
      console.error('Error fetching employees:', err);
      // Ensure employees is always an array even on error
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this employee?')) {
      try {
        await axios.delete(`/employees/${id}`);
        // Filter out the deleted employee from the state
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
      const response = await axios.put(`/employees/${id}`, updatedEmployee);
      const updatedData = response.data;
      
      // Safely update the employees array
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
    fetchEmployees(); // Refresh the list after adding/editing
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
        <h2>👥 Employee Management</h2>
        {user?.role?.name === 'admin' && (
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
              <p>Get started by adding your first employee!</p>
            </div>
          </div>
        ) : (
          employees.map(employee => (
            <div key={employee.id} className="col-lg-4 col-md-6 mb-4">
              <EmployeeCard
                employee={employee}
                onDelete={handleDelete}
                onUpdate={handleUpdate}
              />
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default EmployeeList;
