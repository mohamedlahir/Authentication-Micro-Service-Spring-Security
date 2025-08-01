package com.authentication.service.auth.repositories;

import com.authentication.service.auth.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users,Integer> {

    Users findByusername(String username);
}
