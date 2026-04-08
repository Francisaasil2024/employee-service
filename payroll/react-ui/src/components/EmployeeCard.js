import React, { useState } from 'react';
import EditEmployeeForm from './EditEmployeeForm';
import './EmployeeCard.css';

function EmployeeCard({ employee, onDelete, onUpdate, canEdit = false, canDelete = false }) {
  const [isEditing, setIsEditing] = useState(false);

  const handleEditClick = () => {
    setIsEditing(true);
  };

  const handleEditClose = () => {
    setIsEditing(false);
  };

  const handleSaveEdit = async (updatedEmployee) => {
    await onUpdate(employee.id, updatedEmployee);
    setIsEditing(false);
  };

  if (isEditing) {
    return (
      <EditEmployeeForm 
        employee={employee}
        onSave={handleSaveEdit}
        onCancel={handleEditClose}
      />
    );
  }

  return (
    <div className="card employee-card h-100 shadow-sm">
      <div className="card-body">
        <h5 className="card-title">
          👤 {employee.name}
        </h5>
        <p className="card-text text-muted">
          <strong>ID:</strong> {employee.id}
        </p>
        <p className="card-text">
          <strong>Role:</strong> 
          <span className="badge bg-info ms-2">
            {employee.role?.name || 'No role assigned'}
          </span>
        </p>
      </div>
      <div className="card-footer bg-light">
        {canEdit && (
          <button 
            className="btn btn-sm btn-warning me-2"
            onClick={handleEditClick}
          >
            ✏️ Edit
          </button>
        )}
        {canDelete && (
          <button 
            className="btn btn-sm btn-danger"
            onClick={() => onDelete(employee.id)}
          >
            🗑️ Delete
          </button>
        )}
      </div>
    </div>
  );
}

export default EmployeeCard;
