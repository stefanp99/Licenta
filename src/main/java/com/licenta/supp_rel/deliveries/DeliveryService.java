package com.licenta.supp_rel.deliveries;

import com.licenta.supp_rel.deviations.DeviationRepository;
import com.licenta.supp_rel.deviations.DeviationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DeliveryService {
    @Autowired
    private DeviationService deviationService;
    @Autowired
    DeliveryRepository deliveryRepository;
    @Autowired
    DeviationRepository deviationRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public List<Delivery> findDeliveriesByDate(Date date){
        List<Delivery> deliveries = deliveryRepository.findByStatus(DeliveryStatus.delivered);
        List<Delivery> deliveriesFromDay = new ArrayList<>();
        for(Delivery delivery: deliveries)
            if (dateFormat.format(delivery.getDeliveryDate().getTime()).equals(dateFormat.format(date)))
                deliveriesFromDay.add(delivery);
        return deliveriesFromDay;
    }

    public Delivery dispatchDelivery(Integer id){
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if(delivery != null){
            delivery.setDispatchDate(new Timestamp(System.currentTimeMillis()));
            delivery.setStatus(DeliveryStatus.dispatched);
            deliveryRepository.save(delivery);
            return delivery;
        }
        return null;
    }

    public Delivery deliverDelivery(Integer id, Long realQuantity){
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if(delivery != null){
            delivery.setDeliveryDate(new Timestamp(System.currentTimeMillis()));
            delivery.setStatus(DeliveryStatus.delivered);
            deliveryRepository.save(delivery);
            deviationService.checkDeviations(delivery, realQuantity);
            return delivery;
        }
        return null;
    }
}
