package com.authentication.service.auth.Exceptions;

import com.authentication.service.auth.DTO.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestExceptionController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class))
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // satisfy JWTFilter dependency which requires JWTService when the security filter is picked up
    // Provide a simple JWTService bean via TestConfiguration instead of using @MockBean
    // to avoid inline Mockito/Byte Buddy instrumentation issues on newer JDKs.

    @BeforeEach
    void setup() {
    }

    // Test configuration to supply a real JWTService bean (no mocking)
    @org.springframework.boot.test.context.TestConfiguration
    static class JwtTestConfig {
        @org.springframework.context.annotation.Bean
        public com.authentication.service.auth.service.JWTService jwtService() {
            return new com.authentication.service.auth.service.JWTService();
        }
    }

    @Test
    public void whenValidationFails_shouldReturn400WithFieldErrors() throws Exception {
        String body = "{}"; // missing name

        mockMvc.perform(post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("name")));
    }

    @Test
    public void whenInvalidCredentialsThrown_shouldReturn403() throws Exception {
        mockMvc.perform(get("/test/invalid-credentials"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value("invalid credentials"))
                .andExpect(jsonPath("$.errorCode").value(403));
    }

    @Test
    public void whenExpiredJwtThrown_shouldReturn401() throws Exception {
        mockMvc.perform(get("/test/expired-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value(containsString("token expired")));
    }

    @Test
    public void whenBadCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorMessage").value(containsString("bad credentials")));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void whenAccessDenied_shouldReturn403() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value(containsString("denied")));
    }

    @Test
    public void whenTypeMismatch_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/type-mismatch/notAnInt"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(containsString("Type mismatch")));
    }

    @Test
    public void whenDataAccessThrows_shouldReturn500() throws Exception {
        mockMvc.perform(get("/test/data-access"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorMessage").value(containsString("db failure")));
    }

    @Test
    public void whenIllegalArgThrown_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/illegal-arg"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(containsString("bad arg")));
    }

    @Test
    public void whenUnsupportedMediaType_shouldReturn415() throws Exception {
        mockMvc.perform(post("/test/media")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.errorMessage").value(containsString("Unsupported media type")));
    }

    @Test
    public void whenMalformedJson_shouldReturn400() throws Exception {
        mockMvc.perform(post("/test/malformed")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(containsString("Malformed request")));
    }

    @Test
    public void whenMissingParam_shouldReturn400() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(containsString("Missing request parameter")));
    }
}

