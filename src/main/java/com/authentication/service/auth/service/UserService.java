package com.authentication.service.auth.service;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.JWTResponseToken;
import com.authentication.service.auth.models.Users;
import com.authentication.service.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;


@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    JWTResponseToken responseToken;

    @Bean
    private PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    public List<Users>  getUser(Users users){
        List<Users> userList = userRepository.findAll();
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).setPassword("********");
        }
        return userList;
    }

    public ResponseEntity<String> saveUser(Users user)
    {
        user.setPassword(encoder().encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User is saved");
    }


    public JWTResponseToken verify(AuthenticationModel loginDetails) {
        Authentication authentication =
                authenticationManager.authenticate
                        (new UsernamePasswordAuthenticationToken(loginDetails.getUsername(),loginDetails.getPassword()));

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(loginDetails.getUsername());
            responseToken.setToken(token);
            return responseToken;
        }
        return new JWTResponseToken();
    }
}
