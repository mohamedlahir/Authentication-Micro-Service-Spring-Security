package com.authentication.service.auth.controllers;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.JWTResponseToken;
import com.authentication.service.auth.models.Users;
//import com.authentication.service.auth.repositories.UserRepository;
import com.authentication.service.auth.service.JWTService;
import com.authentication.service.auth.service.MyUserDetails;
import com.authentication.service.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/auth")
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private MyUserDetails userDetailsService;

//    @Autowired
//    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/greet")
    public String greet(){
        return "Hello Lahir!";
    }

    @PostMapping("/register")
    public ResponseEntity<String> saveUser(@RequestBody Users users){
        return userService.saveUser(users);
    }

    @PostMapping("/login")
    public JWTResponseToken login(@RequestBody AuthenticationModel loginDetails) throws NoSuchAlgorithmException {
        return userService.verify(loginDetails);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(false);
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean isValid = jwtService.isTokenValid(token, userDetails);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}
