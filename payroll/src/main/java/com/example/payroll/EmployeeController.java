package com.example.payroll;

import com.example.payroll.role.Role;
import com.example.payroll.role.RoleRepository;
import com.example.payroll.role.RoleNotFoundException;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class EmployeeController {
    
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    EmployeeController(EmployeeRepository employeeRepository, RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/employees")
    List<Employee> all() {
        return employeeRepository.findAll();
    }

    @PostMapping("/employees")
    Employee newEmployee(@RequestBody Employee newEmployee) {
        if (newEmployee.getRole() == null || newEmployee.getRole().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role and role.id are required in request body");
        }

        Long roleId = newEmployee.getRole().getId();
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));

        newEmployee.setRole(role);
        return employeeRepository.save(newEmployee);
    }
    
    @GetMapping("/employees/{id}")
    Employee one(@PathVariable Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @PostMapping("/auth/login")
    LoginResponse login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new LoginResponse(false, null, "Username is required");
        }

        Employee employee = employeeRepository.findByUsername(request.getUsername().trim())
            .orElse(null);

        if (employee == null) {
            return new LoginResponse(false, null, "Invalid username");
        }

        // Password is no longer part of the Employee model.
        return new LoginResponse(true, employee, "Login successful");
    }

    @PutMapping("/employees/{id}")
    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
        
        return employeeRepository.findById(id)
            .map(employee -> {
                employee.setName(newEmployee.getName());
                
                if (newEmployee.getRole() != null) {
                    Long roleId = newEmployee.getRole().getId();
                    Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException(roleId));
                    employee.setRole(role);
                }
                
                return employeeRepository.save(employee);
            })
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @DeleteMapping("/employees/{id}")
    void deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
    }

    public static class LoginRequest {
        private String username;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class LoginResponse {
        private boolean success;
        private Employee employee;
        private String message;

        public LoginResponse() {}

        public LoginResponse(boolean success, Employee employee, String message) {
            this.success = success;
            this.employee = employee;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Employee getEmployee() { return employee; }
        public void setEmployee(Employee employee) { this.employee = employee; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
