package com.licenta.supp_rel.repositories;

import com.licenta.supp_rel.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String emailAddress);
}
