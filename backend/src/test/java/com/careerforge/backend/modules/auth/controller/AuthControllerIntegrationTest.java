package com.careerforge.backend.modules.auth.controller;

import com.careerforge.backend.modules.auth.dto.LoginRequest;
import com.careerforge.backend.modules.auth.dto.RegisterRequest;
import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication Controller Integration Tests (MockMvc + In-Memory JPA)")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should create new user and return JWT token")
    void shouldRegisterNewUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test Candidate");
        request.setEmail("candidate.integration@example.com");
        request.setPassword("SecurePassword123!");
        request.setRole(Role.CANDIDATE);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.email").value("candidate.integration@example.com"))
                .andExpect(jsonPath("$.data.role").value("CANDIDATE"));

        assertThat(userRepository.findByEmail("candidate.integration@example.com")).isPresent();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with duplicate email should return error")
    void shouldRejectDuplicateEmailRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("First Candidate");
        request.setEmail("duplicate@example.com");
        request.setPassword("SecurePassword123!");
        request.setRole(Role.CANDIDATE);

        // First registration succeeds
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate registration must fail with 400 Bad Request
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials should authenticate and return token")
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        // Register user first
        RegisterRequest register = new RegisterRequest();
        register.setName("Login Test");
        register.setEmail("login.test@example.com");
        register.setPassword("CorrectPassword123!");
        register.setRole(Role.RECRUITER);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Attempt login
        LoginRequest login = new LoginRequest();
        login.setEmail("login.test@example.com");
        login.setPassword("CorrectPassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me should return authenticated user profile when Bearer token is provided")
    void shouldReturnCurrentUserProfileWhenAuthenticated() throws Exception {
        // Register user and grab token
        RegisterRequest register = new RegisterRequest();
        register.setName("Me Tester");
        register.setEmail("me.test@example.com");
        register.setPassword("ValidPassword123!");
        register.setRole(Role.CANDIDATE);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).path("data").path("token").asText();

        // Call /api/v1/auth/me with Bearer token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("me.test@example.com"))
                .andExpect(jsonPath("$.data.role").value("CANDIDATE"));
    }
}
