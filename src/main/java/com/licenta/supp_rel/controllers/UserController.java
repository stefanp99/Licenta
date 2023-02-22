package com.licenta.supp_rel.controllers;

import com.licenta.supp_rel.entities.User;
import com.licenta.supp_rel.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping(path = "all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

}
