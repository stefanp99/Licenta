package com.licenta.supp_rel.deliveries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> findByStatus(DeliveryStatus status);
}
