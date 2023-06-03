package com.licenta.supp_rel.deviations;

import com.licenta.supp_rel.deliveries.Delivery;
import com.licenta.supp_rel.tolerances.ToleranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

        return realDeliveryDays < lowerLimit || realDeliveryDays > upperLimit;
    }

    public List<Deviation> findDeviationsByTypeSupplierMaterialPlant(String typeInput, String plantIdInput, String supplierIdInput, String materialCodeInput) {
        List<Deviation> allDeviations = deviationRepository.findAll();
        List<Deviation> matchingDeviations = new ArrayList<>();

        List<String> plantIds = Arrays.asList(plantIdInput.split(","));
        List<String> supplierIds = Arrays.asList(supplierIdInput.split(","));
        List<String> materialCodes = Arrays.asList(materialCodeInput.split(","));
        List<String> types = Arrays.asList(typeInput.split(","));

        for (Deviation deviation : allDeviations) {
            // Check if the deviation matches any of the specified plantIds, supplierIds, and materialCodes
            if ((plantIds.contains(deviation.getDelivery().getContract().getPlant().getId()) || plantIds.contains("*")) &&
                    (supplierIds.contains(deviation.getDelivery().getContract().getSupplier().getId()) || supplierIds.contains("*")) &&
                    (materialCodes.contains(deviation.getDelivery().getContract().getMaterialCode()) || materialCodes.contains("*")) &&
                    (types.contains(deviation.getType().toString()) || types.contains("*"))) {
                // Add the matching deviation to the list
                matchingDeviations.add(deviation);
            }
        }
        return matchingDeviations;
    }

    public List<Deviation> findDeviationsByCreationDate(Date creationDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Deviation> deviations = deviationRepository.findAll();
        List<Deviation> returnedDeviations = new ArrayList<>();
        for(Deviation deviation: deviations)
            if(sdf.format(new Date(deviation.getCreationDate().getTime())).equals(sdf.format(creationDate)))
                returnedDeviations.add(deviation);
        return returnedDeviations;
    }

    public List<Deviation> findDeviationsByTypeSupplierMaterialPlantCreationDate(String typeInput, String plantIdInput,
                                                                                String supplierIdInput, String materialCodeInput,
                                                                                Date creationDate){
        List<Deviation> deviationsByTypeSupplierMaterialPlant =
                findDeviationsByTypeSupplierMaterialPlant(typeInput, plantIdInput, supplierIdInput, materialCodeInput);
        List<Deviation> deviationByCreationDate = findDeviationsByCreationDate(creationDate);

        return deviationsByTypeSupplierMaterialPlant.stream()
                .distinct()
                .filter(deviationByCreationDate::contains).collect(Collectors.toList());
    }
}
