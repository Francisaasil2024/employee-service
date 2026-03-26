package com.example.payroll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PayrollApplication {

	private static final Logger logger = LoggerFactory.getLogger(PayrollApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PayrollApplication.class, args);
	}

	@Bean
	public CommandLineRunner logAllEmployees(EmployeeRepository employeeRepository) {
		return args -> {
			logger.info("=== Employee listing at end of startup ===");
			var employees = employeeRepository.findAll();
			if (employees.isEmpty()) {
				logger.info("No employees found in database.");
			} else {
				employees.forEach(employee -> {
					String roleName = employee.getRole() != null ? employee.getRole().getName() : "(none)";
					Long roleId = employee.getRole() != null ? employee.getRole().getId() : null;
					logger.info("Employee name='{}', username='{}', role='{}', roleId='{}'", employee.getName(), employee.getUsername(), roleName, roleId);
				});
			}
			logger.info("=== Employee listing complete ===");
		};
	}

}
