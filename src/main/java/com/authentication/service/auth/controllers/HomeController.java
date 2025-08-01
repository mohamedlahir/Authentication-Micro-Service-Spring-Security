package com.authentication.service.auth.controllers;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.JWTResponseToken;
import com.authentication.service.auth.models.Users;
import com.authentication.service.auth.repositories.UserRepository;
import com.authentication.service.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String greet(){
        return "Hello World!";
    }

    @PostMapping("/register")
    public ResponseEntity<String> saveUser(@RequestBody Users users){
        return userService.saveUser(users);
    }

    @PostMapping("/login")
    public JWTResponseToken login(@RequestBody AuthenticationModel loginDetails) throws NoSuchAlgorithmException {
        return userService.verify(loginDetails);
    }
}
