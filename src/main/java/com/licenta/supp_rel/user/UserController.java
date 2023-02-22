package com.licenta.supp_rel.user;

import com.licenta.supp_rel.token.Token;
import com.licenta.supp_rel.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;

    @GetMapping(path = "all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping(path = "logged-user")
    public User getLoggedUser(@RequestHeader("Authorization") String authorization){
        String token = authorization.substring(7);
        Optional<Token> tokenObj = tokenRepository.findByToken(token);
        return tokenObj.map(Token::getUser).orElse(null);
    }

}
