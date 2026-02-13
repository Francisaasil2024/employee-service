package com.example.payroll;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.payroll.security.EmployeeUserDetails;
import com.example.payroll.role.Role;
import com.example.payroll.role.RoleRepository;
import com.example.payroll.role.RoleNotFoundException;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginController(AuthenticationManager authenticationManager,
                           EmployeeRepository employeeRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Validate username is not empty
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResponse(false, "Username cannot be empty", null));
            }

            // Check if username already exists (ensure uniqueness)
            if (employeeRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RegisterResponse(false, "Username already exists", null));
            }

            // Validate password is not empty
            if (registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResponse(false, "Password cannot be empty", null));
            }

            // Get role from repository
            String roleName = registerRequest.getRole() != null ? registerRequest.getRole() : "USER";
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));

            // Create new employee with encrypted password
            Employee newEmployee = new Employee();
            newEmployee.setName(registerRequest.getName());
            newEmployee.setUsername(registerRequest.getUsername());
            newEmployee.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newEmployee.setRole(role);

            // Save employee
            Employee savedEmployee = employeeRepository.save(newEmployee);

            // Return response with DTO (without password)
            EmployeeDTO employeeDTO = convertToDTO(savedEmployee);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(true, "Registration successful", employeeDTO));
        } catch (RoleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RegisterResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RegisterResponse(false, "Registration failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate using AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Set authentication in SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get authenticated user details
            EmployeeUserDetails userDetails = (EmployeeUserDetails) authentication.getPrincipal();
            Employee employee = userDetails.getEmployee();

            // Return response with DTO (without password)
            EmployeeDTO employeeDTO = convertToDTO(employee);
            return ResponseEntity.ok(new LoginResponse(true, "Login successful", employeeDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, "Invalid username or password", null));
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            // Get current authentication from SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
                EmployeeUserDetails userDetails = (EmployeeUserDetails) authentication.getPrincipal();
                Employee employee = userDetails.getEmployee();
                
                CurrentUserResponse response = new CurrentUserResponse(
                    employee.getId(),
                    employee.getUsername(),
                    employee.getName(),
                    employee.getRole() != null ? employee.getRole().getName() : "UNKNOWN",
                    authentication.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toList()
                );
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "No user logged in", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "Error retrieving user: " + e.getMessage(), null));
        }
    }

    // Helper method to convert Employee to EmployeeDTO (excludes password)
    private EmployeeDTO convertToDTO(Employee employee) {
        return new EmployeeDTO(
            employee.getId(),
            employee.getName(),
            employee.getUsername(),
            employee.getRole() != null ? employee.getRole().getName() : null,
            employee.getRole() != null ? employee.getRole().getId() : null
        );
    }

    // DTO class for Employee response (without password)
    public static class EmployeeDTO {
        private Long id;
        private String name;
        private String username;
        private String role;
        private Long roleId;

        public EmployeeDTO() {}

        public EmployeeDTO(Long id, String name, String username, String role, Long roleId) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.role = role;
            this.roleId = roleId;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
    }

    // DTO classes
    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {
        private boolean success;
        private String message;
        private EmployeeDTO employee;

        public LoginResponse() {}

        public LoginResponse(boolean success, String message, EmployeeDTO employee) {
            this.success = success;
            this.message = message;
            this.employee = employee;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public EmployeeDTO getEmployee() {
            return employee;
        }

        public void setEmployee(EmployeeDTO employee) {
            this.employee = employee;
        }
    }

    public static class CurrentUserResponse {
        private Long id;
        private String username;
        private String name;
        private String role;
        private java.util.List<String> authorities;

        public CurrentUserResponse() {}

        public CurrentUserResponse(Long id, String username, String name, String role, java.util.List<String> authorities) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.role = role;
            this.authorities = authorities;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public java.util.List<String> getAuthorities() { return authorities; }
        public void setAuthorities(java.util.List<String> authorities) { this.authorities = authorities; }
    }

    public static class RegisterRequest {
        private String name;
        private String username;
        private String password;
        private String role;

        public RegisterRequest() {}

        public RegisterRequest(String name, String username, String password, String role) {
            this.name = name;
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class RegisterResponse {
        private boolean success;
        private String message;
        private EmployeeDTO employee;

        public RegisterResponse() {}

        public RegisterResponse(boolean success, String message, EmployeeDTO employee) {
            this.success = success;
            this.message = message;
            this.employee = employee;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public EmployeeDTO getEmployee() {
            return employee;
        }

        public void setEmployee(EmployeeDTO employee) {
            this.employee = employee;
        }
    }
}