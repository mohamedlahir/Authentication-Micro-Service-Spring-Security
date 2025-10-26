package com.authentication.service.auth.service;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.JWTResponseToken;
import com.authentication.service.auth.models.Users;
import com.authentication.service.auth.repositories.AuthenticationModelRepository;
//import com.authentication.service.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AuthenticationModelRepository AuthenticationModelRepository;

    @Autowired
    private JWTService jwtService;

//    @Autowired
//    private UserRepository userRepository;

    @Autowired
    JWTResponseToken responseToken;

    // changed kafkaTemplate to send String payloads (serialize Users to JSON)
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    public List<AuthenticationModel>  getUser(AuthenticationModel users){
        List<AuthenticationModel> userList = AuthenticationModelRepository.findAll();
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).setPassword("********");
        }
        return userList;
    }

    public ResponseEntity<String> saveUser(Users usersData)
    {
        AuthenticationModel authenticationModel = new AuthenticationModel();
        authenticationModel.setPassword(encoder().encode(usersData.getPassword()));
        String profileIDStr = UUID.randomUUID().toString();
        authenticationModel.setProfileID(profileIDStr);
        authenticationModel.setEmail(usersData.getEmail());
        authenticationModel.setRole(usersData.getRole());
        AuthenticationModelRepository.save(authenticationModel);
        usersData.setProfileID(profileIDStr);
        System.err.println("User is saved: " + usersData.getProfileID());
        // Serialize Users to JSON and send as String to avoid ClassCastException with StringSerializer
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String payload = om.writeValueAsString(usersData);
            kafkaTemplate.send("user-profile-creation", profileIDStr, payload);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return ResponseEntity.status(500).body("Failed to serialize user data for Kafka: " + e.getMessage());
        }

        return ResponseEntity.ok("User is saved");
    }
    public JWTResponseToken verify(AuthenticationModel loginDetails) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate
                            (new UsernamePasswordAuthenticationToken(loginDetails.getEmail(),loginDetails.getPassword()));
            if (authentication.isAuthenticated()) {
                // Get Users entity from principal and use its role
                AuthenticationModel user = AuthenticationModelRepository.findByEmail(loginDetails.getEmail());
                String token = jwtService.generateToken(user.getEmail(), user.getRole());
                responseToken.setToken(token);
                return responseToken;
            }
            return new JWTResponseToken();
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
    }
}
