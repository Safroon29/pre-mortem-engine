package com.premortem.engine.service;

import com.premortem.engine.model.AppUser;
import com.premortem.engine.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public RegistrationService(UserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }

    @Transactional
    public void register(String name, String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Enter your name.");
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@"))
            throw new IllegalArgumentException("Enter a valid email address.");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        if (users.existsByEmailIgnoreCase(normalizedEmail))
            throw new IllegalArgumentException("An account with this email already exists.");
        AppUser user = new AppUser();
        user.setDisplayName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(encoder.encode(password));
        user.setRole("USER");
        users.save(user);
    }
}
