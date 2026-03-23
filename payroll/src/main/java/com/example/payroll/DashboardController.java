package com.example.payroll;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final EmployeeRepository employeeRepository;

    public DashboardController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Get basic dashboard info (employee count)
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        long employeeCount = employeeRepository.count();
        return ResponseEntity.ok(new StatusResponse(employeeCount));
    }

    /**
     * Get all employees summary
     */
    @GetMapping("/employees-summary")
    public ResponseEntity<?> getEmployeesSummary() {
        var employees = employeeRepository.findAll();
        return ResponseEntity.ok(employees);
    }

    public static class StatusResponse {
        private long totalEmployees;

        public StatusResponse(long totalEmployees) {
            this.totalEmployees = totalEmployees;
        }

        
        public long getTotalEmployees() { return totalEmployees; }
    }
}



