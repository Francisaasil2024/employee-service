package com.example.payroll;

import com.example.payroll.role.Role;
import com.example.payroll.role.RoleRepository;
import com.example.payroll.role.RoleNotFoundException;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        
        Long roleId = newEmployee.getRole().getId();
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        
        newEmployee.setRole(role);
        
        // optionally encode password or store as provided
        return employeeRepository.save(newEmployee);
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
                
                if (newEmployee.getPassword() != null && !newEmployee.getPassword().isEmpty()) {
                    employee.setPassword(newEmployee.getPassword());
                }
                
                return employeeRepository.save(employee);
            })
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @DeleteMapping("/employees/{id}")
    void deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
    }}