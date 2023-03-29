package com.licenta.supp_rel.deliveries;

import com.licenta.supp_rel.contracts.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByContract(Contract contract);

    List<Delivery> findByContractAndStatus(Contract contract, DeliveryStatus status);
}
