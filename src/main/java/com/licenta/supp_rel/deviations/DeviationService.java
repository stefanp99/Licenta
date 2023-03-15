package com.licenta.supp_rel.deviations;

import com.licenta.supp_rel.deliveries.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeviationService {
    @Autowired
    DeviationRepository deviationRepository;

    public List<Deviation> checkDeviations(Delivery delivery, Long realQuantity) {
        List<Deviation> deviations = new ArrayList<>();
        if (!realQuantity.equals(delivery.getExpectedQuantity())) {//TODO: add tolerances here
            Deviation deviation = new Deviation();
            if (realQuantity < delivery.getExpectedQuantity())
                deviation.setType(DeviationTypes.qtyMinus);
            else
                deviation.setType(DeviationTypes.qtyPlus);
            deviation.setQuantityDiff(Math.abs(realQuantity - delivery.getExpectedQuantity()));
            deviation.setTimeDiff(Math.abs(delivery.getExpectedDeliveryDate().getTime() - delivery.getDeliveryDate().getTime()) / (24 * 60 * 60 * 1000));
            deviation.setDelivery(delivery);//add check to see if the deviations does not already exist
            deviationRepository.save(deviation);
            deviations.add(deviation);
        }
        if (!(Math.abs(delivery.getExpectedDeliveryDate().getTime() - delivery.getDeliveryDate().getTime()) <= 24 * 60 * 60 * 1000)) {//TODO: add tolerances here
            Deviation deviation = new Deviation();
            if (delivery.getExpectedDeliveryDate().before(delivery.getDeliveryDate()))
                deviation.setType(DeviationTypes.dayPlus);
            else
                deviation.setType(DeviationTypes.dayMinus);
            deviation.setQuantityDiff(Math.abs(realQuantity - delivery.getExpectedQuantity()));
            deviation.setTimeDiff(Math.abs(delivery.getExpectedDeliveryDate().getTime() - delivery.getDeliveryDate().getTime()) / (24 * 60 * 60 * 1000));
            deviation.setDelivery(delivery);
            deviationRepository.save(deviation);
            deviations.add(deviation);
        }
        return deviations;
    }
}
