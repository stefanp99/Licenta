package com.licenta.supp_rel.auth;

import com.licenta.supp_rel.email.EmailService;
import com.licenta.supp_rel.reportChoices.ReportChoiceService;
import com.licenta.supp_rel.security.JwtService;
import com.licenta.supp_rel.token.*;
import com.licenta.supp_rel.user.Role;
import com.licenta.supp_rel.user.User;
import com.licenta.supp_rel.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository usrRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final ReportChoiceService reportChoiceService;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .emailAddress(request.getEmailAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        var savedUser = usrRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);

        reportChoiceService.addReportChoice(savedUser.getUserId(), "*", "*", "*");
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = usrRepository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse newPassword(NewPasswordRequest request) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.getResetPasswordToken()).orElse(null);
        User user = new User();
        if(passwordResetToken != null)//TODO: extinde check-ul pentru tot restul codului de aici
            user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        usrRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public SimpleMailMessage sendEmailResetPassword(HttpServletRequest request, String emailAddress) {
        Optional<User> user = usrRepository.findByEmailAddress(emailAddress);
        String contextPath = request.getScheme() + "://" + request.getServerName() +
                ":" + "4200" + request.getContextPath();
        String uuid = UUID.randomUUID().toString();
        SimpleMailMessage simpleMailMessage =
                constructResetTokenEmail(contextPath, uuid, user.orElse(null));
        emailService.sendMail(simpleMailMessage);

        PasswordResetToken passwordResetToken = new PasswordResetToken(uuid,
                user.orElse(null),
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
        passwordResetTokenRepository.save(passwordResetToken);
        return simpleMailMessage;
    }

    public ResponseEntity<String> validatePasswordResetToken(String passwordResetToken){
        PasswordResetToken passToken = passwordResetTokenRepository.findByToken(passwordResetToken).orElse(null);
        if(passToken == null)
            return ResponseEntity.status(400).body("{\"token\": \"invalid\"}");
        if(passToken.getExpiryDate().before(new Date()))
            return ResponseEntity.status(400).body("{\"token\": \"expired\"}");

        User user = passToken.getUser();
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return ResponseEntity.status(200).body("{\"token\": \"" + jwtToken + "\"}");
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUserId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private SimpleMailMessage constructResetTokenEmail(
            String contextPath, String token, User user) {
        String url = contextPath + "/change-password/" + token;
        return emailService.constructEmail("Reset Password", url, user);
    }

}
