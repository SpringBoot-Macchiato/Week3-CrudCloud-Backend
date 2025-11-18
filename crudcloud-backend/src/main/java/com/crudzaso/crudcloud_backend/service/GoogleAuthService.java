package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.dto.GoogleUserInfo;
import com.crudzaso.crudcloud_backend.dto.LoginResponse;
import com.crudzaso.crudcloud_backend.model.User;
import com.crudzaso.crudcloud_backend.repository.UserRepository;
import com.crudzaso.crudcloud_backend.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${google.client-id}")
    private String googleClientId;

    /**
     * Verifica el token de Google y autentica/registra al usuario
     */
    public LoginResponse authenticateWithGoogle(String idTokenString) {
        try {
            // Verificar el token de Google
            GoogleUserInfo googleUserInfo = verifyGoogleToken(idTokenString);

            if (googleUserInfo == null) {
                throw new RuntimeException("Invalid Google token");
            }

            // Buscar o crear el usuario
            User user = findOrCreateGoogleUser(googleUserInfo);

            // Generar JWT
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

            return new LoginResponse(token, user.getEmail(), user.getRole());

        } catch (Exception e) {
            throw new RuntimeException("Error authenticating with Google: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica el ID token de Google y extrae la información del usuario
     */
    private GoogleUserInfo verifyGoogleToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
        .setAudience(Collections.singletonList(googleClientId))
        .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            return GoogleUserInfo.builder()
                    .googleId(payload.getSubject())
                    .email(payload.getEmail())
                    .emailVerified(payload.getEmailVerified())
                    .name((String) payload.get("name"))
                    .picture((String) payload.get("picture"))
                    .build();
        }

        return null;
    }

    /**
     * Busca un usuario existente por Google ID o email, o crea uno nuevo
     */
    private User findOrCreateGoogleUser(GoogleUserInfo googleUserInfo) {
        // Primero buscar por Google ID
        User user = userRepository.findByGoogleId(googleUserInfo.getGoogleId()).orElse(null);

        if (user != null) {
            // Usuario existente, actualizar información si es necesario
            updateUserInfo(user, googleUserInfo);
            return userRepository.save(user);
        }

        // Buscar por email (por si el usuario se registró antes con email/password)
        user = userRepository.findByEmail(googleUserInfo.getEmail()).orElse(null);

        if (user != null) {
            // Vincular cuenta existente con Google
            user.setGoogleId(googleUserInfo.getGoogleId());
            user.setProvider("GOOGLE");
            user.setPicture(googleUserInfo.getPicture());
            return userRepository.save(user);
        }

        // Crear nuevo usuario
        return createNewGoogleUser(googleUserInfo);
    }

    /**
     * Crea un nuevo usuario con autenticación de Google
     */
    private User createNewGoogleUser(GoogleUserInfo googleUserInfo) {
        User newUser = User.builder()
                .email(googleUserInfo.getEmail())
                .fullName(googleUserInfo.getName())
                .googleId(googleUserInfo.getGoogleId())
                .provider("GOOGLE")
                .picture(googleUserInfo.getPicture())
                .role("USER") // Por defecto es USER
                .password(null) // No tiene password local
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Actualiza la información del usuario desde Google
     */
    private void updateUserInfo(User user, GoogleUserInfo googleUserInfo) {
        if (googleUserInfo.getName() != null && !googleUserInfo.getName().equals(user.getFullName())) {
            user.setFullName(googleUserInfo.getName());
        }
        if (googleUserInfo.getPicture() != null && !googleUserInfo.getPicture().equals(user.getPicture())) {
            user.setPicture(googleUserInfo.getPicture());
        }
    }
}
