package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.model.ConfirmationToken;
import com.example.demo.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findAppUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found with email: " + email));
    }

    public String signUpUser(AppUser appUser) {
        boolean userExists = appUserRepository.findAppUserByEmail(appUser.getEmail())
                .isPresent();
        if (userExists) {
            throw new IllegalStateException("email already taken.");
        }

        appUser.setPassword(bCryptPasswordEncoder.encode(appUser.getPassword()));
        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        // TODO: Send EMAIL

        return token;
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
