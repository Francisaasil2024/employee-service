package com.example.payroll;

import com.example.payroll.role.Role;               
import com.example.payroll.role.RoleRepository;     

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(EmployeeRepository employeeRepository, RoleRepository roleRepository) { 
        
        return args -> {
            
            // Create roles only if they don't already exist
            Role developerRole = roleRepository.findByName("developer")
                .orElseGet(() -> roleRepository.save(new Role("developer")));
            
            Role testerRole = roleRepository.findByName("tester")
                .orElseGet(() -> roleRepository.save(new Role("tester")));
            
            Role adminRole = roleRepository.findByName("admin")
                .orElseGet(() -> roleRepository.save(new Role("admin")));
            
            if (roleRepository.findByName("manager").isEmpty()) {
                roleRepository.save(new Role("manager"));
            }
            
            log.info("Preloaded Roles.");

            // Only create employees if they don't already exist (prevents duplicate key errors)
            if (employeeRepository.findByUsername("bilbo").isEmpty()) {
                Employee bilbo = new Employee("Bilbo Baggins", "bilbo", developerRole);
                log.info("Preloading " + employeeRepository.save(bilbo));
            }
            
            if (employeeRepository.findByUsername("frodo").isEmpty()) {
                Employee frodo = new Employee("Frodo Baggins", "frodo", testerRole);
                log.info("Preloading " + employeeRepository.save(frodo));
            }
            
            if (employeeRepository.findByUsername("admin").isEmpty()) {
                Employee admin = new Employee("Admin User", "admin", adminRole);
                log.info("Preloading " + employeeRepository.save(admin));
            }
        };
    }
}