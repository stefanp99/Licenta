package com.licenta.supp_rel.user;

import com.licenta.supp_rel.token.Token;
import com.licenta.supp_rel.token.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private TokenRepository tokenRepository;

    public User getLoggedUser(String authorization) {
        String token = authorization.substring(7);
        Optional<Token> tokenObj = tokenRepository.findByToken(token);
        return tokenObj.map(Token::getUser).orElse(null);
    }

    public LoggedUserDto getLoggedUserDto(String authorization) {
        String token = authorization.substring(7);
        Optional<Token> tokenObj = tokenRepository.findByToken(token);
        User user = tokenObj.map(Token::getUser).orElse(null);
        LoggedUserDto loggedUserDto = new LoggedUserDto();
        if (user != null)
            loggedUserDto = new LoggedUserDto(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmailAddress());
        return loggedUserDto;
    }
}
