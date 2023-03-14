package com.licenta.supp_rel.deliveries;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;
    @Autowired
    private DeliveryRepository deliveryRepository;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @GetMapping("deliveries-by-date")
    public List<Delivery> getAllDeliveriesByDate(@RequestParam(value = "date", required = false) String date){
        if(date == null) {
            date = dateFormat.format(new Date());
        }
        try {
            return deliveryService.findDeliveriesByDate(dateFormat.parse(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("add-delivery")
        public Delivery addDelivery(@RequestParam("materialCode") String materialCode, @RequestParam("plantId") String plantId,
                                @RequestParam("supplierId") String supplierId, @RequestParam("pricePerUnit") Float pricePerUnit,
                                @RequestParam("quantity") Long quantity){
        Delivery delivery = new Delivery();
        delivery.setMaterialCode(materialCode);
        delivery.setPlantId(plantId);
        delivery.setSupplierId(supplierId);
        delivery.setPricePerUnit(pricePerUnit);
        delivery.setQuantity(quantity);
        delivery.setStatus(DeliveryStatus.undispatched);
        deliveryRepository.save(delivery);
        return delivery;
    }

    @PutMapping("dispatch-delivery")
    public Delivery dispatchDelivery(@RequestParam("id") Integer id){
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if(delivery != null){
            delivery.setDispatchDate(new Timestamp(System.currentTimeMillis()));
            delivery.setStatus(DeliveryStatus.dispatched);
            deliveryRepository.save(delivery);
            return delivery;
        }
        return null;
    }

    @PutMapping("deliver-delivery")
    public Delivery deliverDelivery(@RequestParam("id") Integer id){
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if(delivery != null){
            delivery.setDeliveryDate(new Timestamp(System.currentTimeMillis()));
            delivery.setStatus(DeliveryStatus.delivered);
            deliveryRepository.save(delivery);
            return delivery;
        }
        return null;
    }
}
