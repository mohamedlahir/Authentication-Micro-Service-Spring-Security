package com.authentication.service.auth.repositories;

import com.authentication.service.auth.models.AuthenticationModel;
import com.authentication.service.auth.models.Users;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationModelRepository extends JpaRepository<AuthenticationModel, Integer> {

    AuthenticationModel findByEmail(String email);
}
