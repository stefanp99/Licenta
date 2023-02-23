package com.licenta.supp_rel.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;
    @PostMapping("register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("email-reset-password")
    public ResponseEntity<SimpleMailMessage> resetPassword(HttpServletRequest request,
                                                           @RequestParam("emailAddress") String emailAddress) {
        return ResponseEntity.ok(service.sendEmailResetPassword(request, emailAddress));
    }

    @PostMapping("changePassword")
    public ResponseEntity<String> changePassword(@RequestParam("token") String resetPasswordToken){
        return service.validatePasswordResetToken(resetPasswordToken);
    }

    @PostMapping("newPassword")
    public ResponseEntity<AuthenticationResponse> newPassword(@RequestBody NewPasswordRequest request){
        System.out.println(request);
        return ResponseEntity.ok(service.newPassword(request));
    }

}
