package com.codeforge.auth;

import com.codeforge.auth.dto.AuthResponse;
import com.codeforge.auth.dto.LoginRequest;
import com.codeforge.auth.dto.RegisterRequest;
import com.codeforge.auth.service.AuthService;
import com.codeforge.exception.BadRequestException;
import com.codeforge.security.JwtTokenProvider;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    private JwtTokenProvider tokenProvider;
    private AuthService authService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 86400000L);

        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, tokenProvider);

        sampleUser = new User("alice", "alice@test.com", "encodedPass", Role.ROLE_PARTICIPANT, "MIT");
        sampleUser.setId(10L);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest req = new RegisterRequest("newuser", "new@test.com", "pass123", "MIT", "ROLE_PARTICIPANT");
        req.setConfirmPassword("pass123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        Authentication auth = new UsernamePasswordAuthenticationToken("newuser", "pass123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertEquals("alice", resp.getUsername());
    }

    @Test
    void testRegisterDuplicateUsernameThrowsException() {
        RegisterRequest req = new RegisterRequest("alice", "diff@test.com", "pass123", "MIT", "ROLE_PARTICIPANT");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void testRegisterPasswordMismatchThrowsException() {
        RegisterRequest req = new RegisterRequest("newuser", "new@test.com", "pass123", "MIT", "ROLE_PARTICIPANT");
        req.setConfirmPassword("mismatched");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest("alice", "pass123");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(sampleUser));
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pass123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertEquals(sampleUser.getUsername(), resp.getUsername());
    }

    @Test
    void testLoginInvalidUserThrowsException() {
        LoginRequest req = new LoginRequest("unknown", "pass123");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(req));
    }

    @Test
    void testRegisterRoleAdminIsBlocked() {
        RegisterRequest req = new RegisterRequest("hacker", "hacker@test.com", "pass123", "Hackers Inc", "ROLE_ADMIN");
        req.setConfirmPassword("pass123");

        when(userRepository.existsByUsername("hacker")).thenReturn(false);
        when(userRepository.existsByEmail("hacker@test.com")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("cannot be self-assigned"));
    }

    @Test
    void testJwtTokenGenerationAndValidation() {
        String token = tokenProvider.generateToken(sampleUser.getId(), sampleUser.getUsername(), sampleUser.getRole().name());
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("alice", tokenProvider.getUsernameFromJwt(token));
    }

    @Test
    void testJwtTokenValidationRejectsMalformed() {
        assertFalse(tokenProvider.validateToken("malformed.token.value"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }
}
