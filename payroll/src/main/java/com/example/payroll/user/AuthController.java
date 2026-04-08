package com.example.payroll.user;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.payroll.Employee;
import com.example.payroll.EmployeeRepository;
import com.example.payroll.JwtUtil;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private static UserRepository userRepositoryStatic;
    private static EmployeeRepository employeeRepositoryStatic;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, EmployeeRepository employeeRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.jwtUtil = jwtUtil;
        AuthController.userRepositoryStatic = userRepository;
        AuthController.employeeRepositoryStatic = employeeRepository;
    }

    // ✅ This is called from EmployeeController when new employee is added
    public static void addUser(String username, String password, String fullName) {
        if (userRepositoryStatic != null) {
            // Check if user already exists
            boolean exists = userRepositoryStatic.findByUsername(username).isPresent();
            if (!exists) {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                userRepositoryStatic.save(newUser);
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new LoginResponse(false, "Username and password are required"));
        }

        String trimmedUsername = request.getUsername().trim();
        Optional<User> user = userRepository.findByUsernameAndPassword(trimmedUsername, request.getPassword().trim());

        if (user.isPresent()) {
            String token = jwtUtil.generateToken(trimmedUsername);
            String role = getRoleForUsername(trimmedUsername);
            return ResponseEntity.ok(new LoginResponse(true, "Login successful", token, trimmedUsername, role));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(false, "Invalid username or password"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> profile(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        String username = getUsernameFromHeader(authorizationHeader);
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        String role = employee.getRole() != null ? employee.getRole().getName() : "USER";
        return ResponseEntity.ok(new ProfileResponse(employee.getName(), employee.getUsername(), employee.getEmail(), employee.getDepartment(), role));
    }

    public static UserInfo getUser(String username) {
        if (employeeRepositoryStatic == null) {
            return new UserInfo(username, "USER");
        }
        return employeeRepositoryStatic.findByUsername(username)
                .map(employee -> new UserInfo(employee.getUsername(), employee.getRole() != null ? employee.getRole().getName() : "USER"))
                .orElse(new UserInfo(username, "USER"));
    }

    private String getRoleForUsername(String username) {
        return employeeRepository.findByUsername(username)
                .map(employee -> employee.getRole() != null ? employee.getRole().getName() : "USER")
                .orElse("USER");
    }

    private String getUsernameFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        return jwtUtil.getUsernameFromToken(authorizationHeader.substring(7));
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private boolean success;
        private String message;
        private String token;
        private String username;
        private String role;

        public LoginResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public LoginResponse(boolean success, String message, String token, String username, String role) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.username = username;
            this.role = role;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class ProfileResponse {
        private String fullName;
        private String username;
        private String email;
        private String department;
        private String role;

        public ProfileResponse(String fullName, String username, String email, String department, String role) {
            this.fullName = fullName;
            this.username = username;
            this.email = email;
            this.department = department;
            this.role = role;
        }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class UserInfo {
        private final String username;
        private final String role;

        public UserInfo(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}