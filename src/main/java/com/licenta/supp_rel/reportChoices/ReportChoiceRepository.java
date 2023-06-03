package com.licenta.supp_rel.reportChoices;

import com.licenta.supp_rel.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportChoiceRepository extends JpaRepository<ReportChoice, Integer> {

    Optional<ReportChoice> findByUser(User user);

}
