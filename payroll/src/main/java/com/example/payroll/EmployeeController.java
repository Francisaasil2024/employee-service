package com.example.payroll;

import com.example.payroll.role.Role;
import com.example.payroll.role.RoleRepository;
import com.example.payroll.role.RoleNotFoundException;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.payroll.security.EmployeeUserDetails;

@RestController
class EmployeeController {
    
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    EmployeeController(EmployeeRepository employeeRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'TESTER', 'MANAGER')")
    List<Employee> all() {
        return employeeRepository.findAll();
    }

    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    Employee newEmployee(@RequestBody Employee newEmployee) {
        
        Long roleId = newEmployee.getRole().getId();
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        
        newEmployee.setRole(role);
        
        // Hash password before saving
        if (newEmployee.getPassword() != null && !newEmployee.getPassword().isEmpty()) {
            newEmployee.setPassword(passwordEncoder.encode(newEmployee.getPassword()));
        }
        
        return employeeRepository.save(newEmployee);
    }
    
    @GetMapping("/employees/{id}")
    Employee one(@PathVariable Long id) {
        // Get current logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        EmployeeUserDetails currentUser = (EmployeeUserDetails) authentication.getPrincipal();
        Employee loggedInEmployee = currentUser.getEmployee();
        
        // Check if user is ADMIN or owns this record
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !loggedInEmployee.getId().equals(id)) {
            throw new AccessDeniedException("You can only view your own employee record");
        }
        
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @PutMapping("/employees/{id}")
    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
        
        // Get current logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        EmployeeUserDetails currentUser = (EmployeeUserDetails) authentication.getPrincipal();
        Employee loggedInEmployee = currentUser.getEmployee();
        
        // Check if user is ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // Authorization check: non-admin users can only edit their own record
        if (!isAdmin && !loggedInEmployee.getId().equals(id)) {
            throw new AccessDeniedException("You can only edit your own employee record");
        }
        
        return employeeRepository.findById(id)
            .map(employee -> {
                // Update name (allowed for all authorized users)
                employee.setName(newEmployee.getName());
                
                // Only allow role updates if user is ADMIN
                // Non-admin users cannot change their own role
                if (isAdmin && newEmployee.getRole() != null) {
                    Long roleId = newEmployee.getRole().getId();
                    Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException(roleId));
                    employee.setRole(role);
                }
                // If not admin, keep the existing role (ignore role from request body)
                
                // Update password if provided (encode before saving)
                if (newEmployee.getPassword() != null && !newEmployee.getPassword().isEmpty()) {
                    employee.setPassword(passwordEncoder.encode(newEmployee.getPassword()));
                }
                
                return employeeRepository.save(employee);
            })
            .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    void deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
    }
}