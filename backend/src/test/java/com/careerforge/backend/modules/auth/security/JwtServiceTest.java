package com.careerforge.backend.modules.auth.security;

import com.careerforge.backend.modules.auth.model.Role;
import com.careerforge.backend.modules.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT Service Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", TEST_EXPIRATION_MS);

        testUser = new User("Alice Developer", "alice@example.com", "encodedPassword", Role.CANDIDATE);
        ReflectionTestUtils.setField(testUser, "id", 42L);
    }

    @Test
    @DisplayName("Should generate valid JWT token with subject and custom claims")
    void shouldGenerateValidTokenWithClaims() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature

        String extractedEmail = jwtService.extractEmail(token);
        assertThat(extractedEmail).isEqualTo("alice@example.com");

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertThat(role).isEqualTo("CANDIDATE");

        Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    @DisplayName("Should validate token against matching UserDetails")
    void shouldValidateTokenAgainstMatchingUserDetails() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "alice@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
        );

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject token when validated against mismatched UserDetails")
    void shouldRejectTokenWhenEmailMismatch() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        UserDetails wrongUserDetails = new org.springframework.security.core.userdetails.User(
                "bob@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_RECRUITER"))
        );

        // Act
        boolean isValid = jwtService.isTokenValid(token, wrongUserDetails);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject tampered token with invalid signature")
    void shouldRejectTamperedToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        String tamperedToken = token + "xyz";

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "alice@example.com",
                "encodedPassword",
                List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
        );

        // Act
        boolean isValid = jwtService.isTokenValid(tamperedToken, userDetails);

        // Assert
        assertThat(isValid).isFalse();
    }
}
