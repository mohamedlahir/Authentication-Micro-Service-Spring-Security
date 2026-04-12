package com.authentication.service.auth.Exceptions;

import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    public static class TestDTO {
        @NotBlank(message = "name must not be blank")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @PostMapping("/validate")
    public String validateBody(@Valid @RequestBody TestDTO dto) {
        return "ok";
    }

    @GetMapping("/invalid-credentials")
    public void invalidCredentials() {
        throw new InvalidCredentialsException("invalid credentials");
    }

    @GetMapping("/expired-jwt")
    public void expiredJwt() {
        throw new io.jsonwebtoken.ExpiredJwtException(null, null, "token expired");
    }

    @GetMapping("/bad-credentials")
    public void badCredentials() {
        throw new BadCredentialsException("bad credentials");
    }

    @GetMapping("/access-denied")
    public void accessDenied() {
        throw new AccessDeniedException("denied");
    }

    @GetMapping("/type-mismatch/{id}")
    public String typeMismatch(@PathVariable Integer id) {
        return "id:" + id;
    }

    @GetMapping("/data-access")
    public void dataAccess() {
        throw new DataAccessResourceFailureException("db failure");
    }

    @GetMapping("/illegal-arg")
    public void illegalArg() {
        throw new IllegalArgumentException("bad arg");
    }

    @PostMapping(value = "/media", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String media(@RequestBody TestDTO dto) {
        return "ok";
    }

    @PostMapping("/malformed")
    public String malformed(@RequestBody TestDTO dto) {
        return "ok";
    }

    @GetMapping("/missing-param")
    public String missingParam(@RequestParam("req") String req) {
        return req;
    }
}

