package com.licenta.supp_rel.deliveries;

import com.licenta.supp_rel.contracts.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private ContractRepository contractRepository;
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
        public Delivery addDelivery(@RequestParam("expectedQuantity") Long expectedQuantity,
                                    @RequestParam("expectedDeliveryDate") Timestamp expectedDeliveryDate,
                                    @RequestParam("contractId") Integer contractId){
        Delivery delivery = new Delivery();
        delivery.setExpectedQuantity(expectedQuantity);
        delivery.setExpectedDeliveryDate(expectedDeliveryDate);
        delivery.setContract(contractRepository.findById(contractId).orElse(null));
        delivery.setStatus(DeliveryStatus.undispatched);
        deliveryRepository.save(delivery);
        return delivery;
    }

    @PutMapping("dispatch-delivery")
    public Delivery dispatchDelivery(@RequestParam("id") Integer id){
        return deliveryService.dispatchDelivery(id);
    }

    @PutMapping("deliver-delivery")
    public ResponseEntity<DeliveryResponse> deliverDelivery(@RequestParam("id") Integer id, @RequestParam("realQuantity") Long realQuantity){
        return ResponseEntity.ok(deliveryService.deliverDelivery(id, realQuantity));
    }
}
