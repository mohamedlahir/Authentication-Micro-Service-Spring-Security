package com.authentication.service.auth.service;

import com.authentication.service.auth.models.Users;
import com.authentication.service.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetails implements UserDetailsService {

    @Autowired
    UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users user = repository.findByusername(username);

        System.out.println(user.getUsername());

        if (user == null) {
            throw new UsernameNotFoundException("user not found" + user);
        }

        return new UserPrincipal(user);

    }
}
