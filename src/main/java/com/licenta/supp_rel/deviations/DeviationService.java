package com.licenta.supp_rel.deviations;

import com.licenta.supp_rel.deliveries.Delivery;
import com.licenta.supp_rel.tolerances.ToleranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeviationService {
    @Autowired
    DeviationRepository deviationRepository;

    @Autowired
    private ToleranceService toleranceService;

    public List<Deviation> checkDeviations(Delivery delivery, Long realQuantity) {
        List<Deviation> deviations = new ArrayList<>();
        //qty check
        if (isQtyDeviation(delivery, realQuantity)) {
            Deviation deviation = new Deviation();
            if (realQuantity < delivery.getExpectedQuantity())
                deviation.setType(DeviationTypes.qtyMinus);
            else
                deviation.setType(DeviationTypes.qtyPlus);
            deviation.setQuantityDiff((float) Math.abs(realQuantity - delivery.getExpectedQuantity()) * 100 / delivery.getExpectedQuantity());
            deviation.setTimeDiff(Math.abs(delivery.getExpectedDeliveryDate().getTime() - delivery.getDeliveryDate().getTime()) / (24 * 60 * 60 * 1000));
            deviation.setDelivery(delivery);
            deviation.setCreationDate(new Timestamp(System.currentTimeMillis()));
            if (!deviationExists(deviation)) {
                deviationRepository.save(deviation);
                deviations.add(deviation);
            }
        }
        //day check
        if (isDayDeviation(delivery)) {
            Deviation deviation = new Deviation();
            if (delivery.getExpectedDeliveryDate().before(delivery.getDeliveryDate()))
                deviation.setType(DeviationTypes.dayPlus);
            else
                deviation.setType(DeviationTypes.dayMinus);
            deviation.setQuantityDiff((float) Math.abs(realQuantity - delivery.getExpectedQuantity()) * 100 / delivery.getExpectedQuantity());
            deviation.setTimeDiff(Math.abs(delivery.getExpectedDeliveryDate().getTime() - delivery.getDeliveryDate().getTime()) / (24 * 60 * 60 * 1000));
            deviation.setDelivery(delivery);
            deviation.setCreationDate(new Timestamp(System.currentTimeMillis()));
            if (!deviationExists(deviation)) {
                deviationRepository.save(deviation);
                deviations.add(deviation);
            }
        }
        return deviations;
    }

    private boolean deviationExists(Deviation deviation) {
        List<Deviation> allDeviations = deviationRepository.findAll();
        for (Deviation existingDeviation : allDeviations)
            if (deviation.getDelivery().getId().equals(existingDeviation.getDelivery().getId()) &&
                    deviation.getType().equals(existingDeviation.getType()) &&
                    deviation.getQuantityDiff().intValue() == existingDeviation.getQuantityDiff().intValue() &&
                    deviation.getTimeDiff().equals(existingDeviation.getTimeDiff()))
                return true;
        return false;
    }

    boolean isQtyDeviation(Delivery delivery, Long realQuantity){
        long upperLimit = (long) (delivery.getExpectedQuantity() + delivery.getExpectedQuantity()*
                        toleranceService
                                .getUpperQtyToleranceByPlantIdSupplierIdMaterialCode(
                                        delivery.getContract().getPlant().getId(),
                                        delivery.getContract().getSupplier().getId(),
                                        delivery.getContract().getMaterialCode())/100);
        long lowerLimit = (long) (delivery.getExpectedQuantity() - delivery.getExpectedQuantity()*
                toleranceService
                        .getLowerQtyToleranceByPlantIdSupplierIdMaterialCode(
                                delivery.getContract().getPlant().getId(),
                                delivery.getContract().getSupplier().getId(),
                                delivery.getContract().getMaterialCode())/100);

//        System.out.println("real qty:" + realQuantity);
//        System.out.println("upper limit qty: " + upperLimit);
//        System.out.println("lower limit qty: " + lowerLimit);
//        System.out.println("is qty devi: " + (realQuantity < lowerLimit || realQuantity > upperLimit));
        return realQuantity < lowerLimit || realQuantity > upperLimit;
    }

    private boolean isDayDeviation(Delivery delivery) {
        long realDeliveryDays;
        if (delivery.getDeliveryDate().before(delivery.getExpectedDeliveryDate()))
            realDeliveryDays = -Math.abs(delivery.getDeliveryDate().getTime() - delivery.getExpectedDeliveryDate().getTime())
                    / (24 * 60 * 60 * 1000);
        else
            realDeliveryDays = Math.abs(delivery.getDeliveryDate().getTime() - delivery.getExpectedDeliveryDate().getTime())
                    / (24 * 60 * 60 * 1000);
        long upperLimit = toleranceService
                .getUpperDayToleranceByPlantIdSupplierIdMaterialCode(
                        delivery.getContract().getPlant().getId(),
                        delivery.getContract().getSupplier().getId(),
                        delivery.getContract().getMaterialCode());
        long lowerLimit = -toleranceService
                .getLowerDayToleranceByPlantIdSupplierIdMaterialCode(
                        delivery.getContract().getPlant().getId(),
                        delivery.getContract().getSupplier().getId(),
                        delivery.getContract().getMaterialCode());

//        System.out.println("real day:" + realDeliveryDays);
//        System.out.println("upper limit day: " + upperLimit);
//        System.out.println("lower limit day: " + lowerLimit);
//        System.out.println("is day devi: " + (realDeliveryDays < lowerLimit || realDeliveryDays > upperLimit));
        return realDeliveryDays < lowerLimit || realDeliveryDays > upperLimit;
    }
}
