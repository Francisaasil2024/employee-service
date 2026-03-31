package com.example.payroll;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.payroll.role.Role;
import com.example.payroll.role.RoleNotFoundException;
import com.example.payroll.role.RoleRepository;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    public EmployeeController(EmployeeRepository employeeRepository, RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/employees")
    List<Employee> all() {
        return employeeRepository.findAll();
    }

    @PostMapping("/employees")
    Employee newEmployee(@RequestBody EmployeeRequest newEmployee) {
        if (newEmployee.getName() == null || newEmployee.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee name is required");
        }
        if (newEmployee.getUsername() == null || newEmployee.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee username is required");
        }
        if (newEmployee.getRoleId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee roleId is required");
        }

        Role role = roleRepository.findById(newEmployee.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException(newEmployee.getRoleId()));

        Employee employee = new Employee(newEmployee.getName().trim(), newEmployee.getUsername().trim(), role);
        return employeeRepository.save(employee);
    }

    public static class EmployeeRequest {
        private String name;
        private String username;
        private Long roleId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
    }

    @GetMapping("/employees/{id}")
    Employee one(@PathVariable Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
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
}