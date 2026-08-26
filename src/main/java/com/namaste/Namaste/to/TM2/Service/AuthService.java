package com.namaste.Namaste.to.TM2.Service;

import com.namaste.Namaste.to.TM2.Model.User;
import com.namaste.Namaste.to.TM2.Repository.UserRepository;
import com.namaste.Namaste.to.TM2.Request.LoginRequest;
import com.namaste.Namaste.to.TM2.Request.RegisterRequest;
import com.namaste.Namaste.to.TM2.Response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        log.info("Registered new user: {}", user.getUsername());

        return buildAuthResponse(user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        log.info("User logged in: {}", request.getUsername());

        return buildAuthResponse(request.getUsername());
    }

    private AuthResponse buildAuthResponse(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(username)
                .expiresInMs(jwtService.getExpirationMs())
                .build();
    }
}
