package com.licenta.supp_rel.deviations;

import com.licenta.supp_rel.deliveries.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviationRepository extends JpaRepository<Deviation, Integer> {
    List<Deviation> findByTypeAndDeliveryAndQuantityDiffAndTimeDiff(DeviationTypes type, Delivery delivery, Float quantityDiff, Long timeDiff);

    List<Deviation> findByType(DeviationTypes type);
}
