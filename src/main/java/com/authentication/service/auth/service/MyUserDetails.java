package com.authentication.service.auth.service;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.Users;
import com.authentication.service.auth.repositories.AuthenticationModelRepository;
//import com.authentication.service.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetails implements UserDetailsService {

    @Autowired
    AuthenticationModelRepository authenticationModelRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        AuthenticationModel authenticationModel = authenticationModelRepository.findByEmail(username);

        if (authenticationModel == null) {
            throw new UsernameNotFoundException("user not found" + authenticationModel);
        }
        return new UserPrincipal(authenticationModel);

    }
}
