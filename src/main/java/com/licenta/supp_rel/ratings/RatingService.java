package com.licenta.supp_rel.ratings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licenta.supp_rel.contracts.Contract;
import com.licenta.supp_rel.contracts.ContractService;
import com.licenta.supp_rel.deliveries.Delivery;
import com.licenta.supp_rel.deliveries.DeliveryService;
import com.licenta.supp_rel.deviations.Deviation;
import com.licenta.supp_rel.deviations.DeviationRepository;
import com.licenta.supp_rel.deviations.DeviationTypes;
import com.licenta.supp_rel.suppliers.Supplier;
import com.licenta.supp_rel.suppliers.SupplierRepository;
import com.licenta.supp_rel.systemConfigurations.RatingsWeightageDTO;
import com.licenta.supp_rel.systemConfigurations.SystemConfiguration;
import com.licenta.supp_rel.systemConfigurations.SystemConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RatingService {
    @Autowired
    SystemConfigurationRepository systemConfigurationRepository;
    @Autowired
    DeliveryService deliveryService;
    @Autowired
    ContractService contractService;
    @Autowired
    SupplierRepository supplierRepository;
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    DeviationRepository deviationRepository;

    List<Rating> createRatings(String supplierId, String materialCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SystemConfiguration> systemConfigurations = systemConfigurationRepository.findAllByConfigGroupAndConfigName("ratings", "ratings_weightage");
        SystemConfiguration systemConfiguration = null;
        RatingsWeightageDTO ratingsWeightageDTO;
        if (systemConfigurations.size() > 0)
            systemConfiguration = systemConfigurations.get(0);
        if (systemConfiguration != null) {
            try {
                ratingsWeightageDTO = objectMapper.readValue(systemConfiguration.getConfigValues(), RatingsWeightageDTO.class);
                Supplier supplier = null;
                if (supplierId != null)
                    supplier = supplierRepository.findById(supplierId).orElse(null);
                List<Rating> ratings = new ArrayList<>();

                List<Supplier> suppliers;
                if (supplier == null)
                    suppliers = supplierRepository.findAll();
                else
                    suppliers = List.of(supplier);

                for (Supplier supp : suppliers) {
                    List<String> materialCodes = new ArrayList<>();
                    if (materialCode == null || materialCode.isEmpty()) {
                        materialCodes = contractService.findMaterialCodesBySupplier(supp);
                        materialCodes.add(null);
                    } else
                        materialCodes.add(materialCode);
                    for (String matCode : materialCodes) {
                        Rating rating = getRatingBySupplierAndMaterial(supp, matCode, ratingsWeightageDTO);
                        if (rating != null) {
                            if (ratingRepository.existsBySupplierAndMaterialCodeAndQtyPercentageRatingAndQtyNrDevisRatingAndDayPercentageRatingAndDayNrDevisRating
                                    (rating.getSupplier().getId(), rating.getMaterialCode())) {
                                ratingRepository.findBySupplierAndMaterialCode(rating.getSupplier(),
                                        rating.getMaterialCode()).ifPresent(existingRating -> {
                                    rating.setId(existingRating.getId());
                                    ratingRepository.save(rating);
                                });

                            } else {
                                ratings.add(rating);
                                ratingRepository.save(rating);
                            }
                        }
                    }
                }
                curveRating();
                return ratings;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private Rating getRatingBySupplierAndMaterial(Supplier supplier, String materialCode, RatingsWeightageDTO ratingsWeightageDTO) {
        float medPercQtyMinus = 0F;
        float medPercQtyPlus = 0F;

        float medDaysDayMinus = 0F;
        float medDaysDayPlus = 0F;

        int qtyMinusDeviNumber = 0;
        int qtyPlusDeviNumber = 0;
        int dayMinusDeviNumber = 0;
        int dayPlusDeviNumber = 0;

        int correctDeliveriesNr = 0;

        List<Delivery> deliveries = deliveryService.findAllBySupplierIdAndMaterialCodeAndStatus(supplier, materialCode, "delivered");
        int totalDeliveriesNumber = deliveries.size();

        if (totalDeliveriesNumber < ratingsWeightageDTO.getMinDeliveries())
            return null;

        float totalTimeDifferenceInHours = 0F;
        for (Delivery delivery : deliveries) {
            float timeDifferenceInHours;
            timeDifferenceInHours = Math.abs((delivery.getDeliveryDate().getTime() - delivery.getDispatchDate().getTime())/(float)(60 * 60 * 1000));
            totalTimeDifferenceInHours += timeDifferenceInHours;
            List<Deviation> foundDeviations = deviationRepository.findByDelivery(delivery);
            if (foundDeviations.size() == 0)
                correctDeliveriesNr++;
            for (Deviation deviation : foundDeviations) {
                if (deviation.getType().equals(DeviationTypes.qtyMinus)) {
                    medPercQtyMinus += deviation.getQuantityDiff();
                    qtyMinusDeviNumber++;
                } else if (deviation.getType().equals(DeviationTypes.qtyPlus)) {
                    medPercQtyPlus += deviation.getQuantityDiff();
                    qtyPlusDeviNumber++;
                } else if (deviation.getType().equals(DeviationTypes.dayMinus)) {
                    medDaysDayMinus += deviation.getTimeDiff();
                    dayMinusDeviNumber++;
                } else if (deviation.getType().equals(DeviationTypes.dayPlus)) {
                    medDaysDayPlus += deviation.getTimeDiff();
                    dayPlusDeviNumber++;
                }
            }
        }

        if (qtyMinusDeviNumber != 0)
            medPercQtyMinus /= qtyMinusDeviNumber;
        if (qtyPlusDeviNumber != 0)
            medPercQtyPlus /= qtyPlusDeviNumber;

        if (dayMinusDeviNumber != 0)
            medDaysDayMinus /= dayMinusDeviNumber;
        if (dayPlusDeviNumber != 0)
            medDaysDayPlus /= dayPlusDeviNumber;

        float ratingPercentageQty = ratingsWeightageDTO.getMedPercQtyMinusWeight() * medPercQtyMinus +
                ratingsWeightageDTO.getMedPercQtyPlusWeight() * medPercQtyPlus;//lower is better(0-inf)
        float ratingNrDeviationsQty = 1 - (ratingsWeightageDTO.getQtyMinusDeviNumberWeight() * qtyMinusDeviNumber / totalDeliveriesNumber +
                ratingsWeightageDTO.getQtyPlusDeviNumberWeight() * qtyPlusDeviNumber / totalDeliveriesNumber);//higher is better (1-0)

        float ratingPercentageDay = ratingsWeightageDTO.getMedDaysDayMinusWeight() * medDaysDayMinus +
                ratingsWeightageDTO.getMedDaysDayPlusWeight() * medDaysDayPlus;//lower is better(0-inf)
        float ratingNrDeviationsDay = 1 - (ratingsWeightageDTO.getDayMinusDeviNumberWeight() * dayMinusDeviNumber / totalDeliveriesNumber +
                ratingsWeightageDTO.getDayPlusDeviNumberWeight() * dayPlusDeviNumber / totalDeliveriesNumber);//higher is better (1-0)

        Rating rating = new Rating();
        rating.setQtyPercentageRating(ratingPercentageQty);
        rating.setQtyNrDevisRating(ratingNrDeviationsQty);
        rating.setDayPercentageRating(ratingPercentageDay);
        rating.setDayNrDevisRating(ratingNrDeviationsDay);
        rating.setTotalNumberDeliveries(totalDeliveriesNumber);
        rating.setCorrectDeliveriesPercentage((float) correctDeliveriesNr / totalDeliveriesNumber);
        rating.setSupplier(supplier);
        rating.setAverageNumberOfHoursToDeliver(totalTimeDifferenceInHours /deliveries.size());
        if (materialCode == null || materialCode.equals(""))
            rating.setMaterialCode("all");
        else {
            rating.setMaterialCode(materialCode);
            Float averagePrice = contractService.getAveragePriceByMaterialCode(materialCode);
            Contract contract = contractService.findContractBySupplierAndMaterialCode(supplier, materialCode);
            rating.setPriceDeviationPercentage((contract.getPricePerUnit()-averagePrice)*100/averagePrice);
        }
        return rating;
    }

    private void curveRating() {
        List<String> allMaterials = new ArrayList<>();
        List<Rating> ratings = ratingRepository.findAll();
        for (Rating rating : ratings) {
            if (!allMaterials.contains(rating.getMaterialCode()))
                allMaterials.add(rating.getMaterialCode());
        }

        for (String material : allMaterials) {
            Float maxQty = Float.MIN_VALUE;
            Float maxDay = Float.MIN_VALUE;
            for (Rating rating : ratings) {
                if (rating.getMaterialCode().equals(material)) {
                    if (rating.getQtyPercentageRating() > maxQty)
                        maxQty = rating.getQtyPercentageRating();
                    if (rating.getDayPercentageRating() > maxDay)
                        maxDay = rating.getDayPercentageRating();
                }
            }

            for (Rating rating : ratings) {
                if (rating.getMaterialCode().equals(material)) {
                    rating.setQtyDeviationCurveRating(1 - (rating.getQtyPercentageRating() / maxQty));
                    rating.setDayDeviationCurveRating(1 - (rating.getDayPercentageRating() / maxDay));
                    ratingRepository.save(rating);
                }
            }
        }
    }

}
