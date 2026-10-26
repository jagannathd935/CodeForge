package com.codeforge.auth.service;

import com.codeforge.auth.dto.AuthResponse;
import com.codeforge.auth.dto.LoginRequest;
import com.codeforge.auth.dto.RegisterRequest;
import com.codeforge.exception.BadRequestException;
import com.codeforge.security.JwtTokenProvider;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password confirmation does not match!");
        }

        Role role = Role.ROLE_PARTICIPANT;
        if ("ROLE_ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new BadRequestException("Administrative role cannot be self-assigned during registration. Admin privileges must be granted by an authorized administrator.");
        } else if ("ROLE_ORGANIZER".equalsIgnoreCase(request.getRole())) {
            role = Role.ROLE_ORGANIZER;
        }

        String organization = request.getOrganization() != null && !request.getOrganization().isBlank()
                ? request.getOrganization().trim()
                : "CodeForge Tech";

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role,
                organization
        );

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole().name());

        return new AuthResponse(jwt, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole().name(), savedUser.getAvatarUrl(), savedUser.getAuthProvider(), savedUser.getOrganization());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BadRequestException("Invalid username/email or password"));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BadRequestException("Your account has been deactivated by an administrator. Please contact support.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        return new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), user.getAvatarUrl(), user.getAuthProvider(), user.getOrganization());
    }
}

