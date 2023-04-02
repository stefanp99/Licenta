package com.licenta.supp_rel.deliveries;

import com.licenta.supp_rel.contracts.Contract;
import com.licenta.supp_rel.contracts.ContractService;
import com.licenta.supp_rel.deviations.DeviationRepository;
import com.licenta.supp_rel.deviations.DeviationService;
import com.licenta.supp_rel.plants.PlantRepository;
import com.licenta.supp_rel.suppliers.Supplier;
import com.licenta.supp_rel.suppliers.SupplierRepository;
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
    @Autowired
    ContractService contractService;
    @Autowired
    SupplierRepository supplierRepository;
    @Autowired
    PlantRepository plantRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public List<Delivery> findDeliveriesByDate(Date date) {
        List<Delivery> deliveries = deliveryRepository.findByStatus(DeliveryStatus.delivered);
        List<Delivery> deliveriesFromDay = new ArrayList<>();
        for (Delivery delivery : deliveries)
            if (dateFormat.format(delivery.getDeliveryDate().getTime()).equals(dateFormat.format(date)))
                deliveriesFromDay.add(delivery);
        return deliveriesFromDay;
    }

    public Delivery dispatchDelivery(Integer id, Timestamp dispatchDate) {
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if (delivery != null) {
            delivery.setDispatchDate(dispatchDate);
            delivery.setStatus(DeliveryStatus.dispatched);
            deliveryRepository.save(delivery);
            return delivery;
        }
        return null;
    }

    public DeliveryResponse deliverDelivery(Integer id, Long realQuantity, Timestamp deliveryDate) {
        DeliveryResponse deliveryResponse = new DeliveryResponse();
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if (delivery != null) {
            delivery.setDeliveryDate(deliveryDate);
            delivery.setStatus(DeliveryStatus.delivered);
            delivery.setRealQuantity(realQuantity);
            deliveryRepository.save(delivery);
            deliveryResponse.setDelivery(delivery);
            deliveryResponse.setDeviations(deviationService.checkDeviations(delivery, realQuantity));
            return deliveryResponse;
        }
        return null;
    }

    public List<Delivery> findAllBySupplierAndMaterialCodeAndPlantIdAndStatus(Supplier supplier, String materialCode, String plantId, String status) {
        List<Contract> contracts;
        if(plantId != null)
            contracts = contractService.findContractsBySupplierAndMaterialCodeAndPlantNoRepeat(supplier, materialCode, plantRepository.findById(plantId).orElse(null));
        else
            contracts = contractService.findContractsBySupplierAndMaterialCodeAndPlantNoRepeat(supplier, materialCode, null);
        List<Delivery> deliveries = new ArrayList<>();
        for (Contract contract : contracts) {
            List<Delivery> deliveriesFound = deliveryRepository.findByContractAndStatus(contract, DeliveryStatus.valueOf(status));
            if (!deliveriesFound.isEmpty())
                deliveries.addAll(deliveriesFound);
        }
        return deliveries;
    }
}
