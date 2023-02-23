package com.licenta.supp_rel.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping(path = "all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping(path = "logged-user")
    public User getLoggedUser(@RequestHeader("Authorization") String authorization){
        return userService.getLoggedUser(authorization);
    }

    @GetMapping(path = "logged-user-dto")
    public LoggedUserDto getLoggedUserDto(@RequestHeader("Authorization") String authorization){
        return userService.getLoggedUserDto(authorization);
    }

}
