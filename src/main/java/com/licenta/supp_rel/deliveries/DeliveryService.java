package com.licenta.supp_rel.deliveries;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DeliveryService {
    @Autowired
    DeliveryRepository deliveryRepository;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public List<Delivery> findDeliveriesByDate(Date date){
        List<Delivery> deliveries = deliveryRepository.findByStatus(DeliveryStatus.delivered);
        List<Delivery> deliveriesFromDay = new ArrayList<>();
        for(Delivery delivery: deliveries)
            if (dateFormat.format(delivery.getDeliveryDate().getTime()).equals(dateFormat.format(date)))
                deliveriesFromDay.add(delivery);
        return deliveriesFromDay;
    }
}
