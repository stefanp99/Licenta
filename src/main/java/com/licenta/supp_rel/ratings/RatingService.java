package com.licenta.supp_rel.ratings;

import com.licenta.supp_rel.contracts.ContractService;
import com.licenta.supp_rel.deliveries.Delivery;
import com.licenta.supp_rel.deliveries.DeliveryService;
import com.licenta.supp_rel.deviations.Deviation;
import com.licenta.supp_rel.deviations.DeviationRepository;
import com.licenta.supp_rel.deviations.DeviationTypes;
import com.licenta.supp_rel.suppliers.Supplier;
import com.licenta.supp_rel.suppliers.SupplierRepository;
import com.licenta.supp_rel.systemConfigurations.SystemConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RatingService {
    @Autowired
    SystemConfigurationService systemConfigurationService;
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
    private int minDeliveries;
    private float medPercQtyMinusWeight;
    private float medPercQtyPlusWeight;
    private float qtyMinusDeviNumberWeight;
    private float qtyPlusDeviNumberWeight;
    private float medDaysDayMinusWeight;
    private float medDaysDayPlusWeight;
    private float dayMinusDeviNumberWeight;
    private float dayPlusDeviNumberWeight;
//    private float priceWeight;

    List<Rating> createRatings(String supplierId, String materialCode) {
        minDeliveries = Integer.parseInt(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("minDeliveries"))).get(0));
        medPercQtyMinusWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("medPercQtyMinusWeight"))).get(0));
        medPercQtyPlusWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("medPercQtyPlusWeight"))).get(0));
        qtyMinusDeviNumberWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("qtyMinusDeviNumberWeight"))).get(0));
        qtyPlusDeviNumberWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("qtyPlusDeviNumberWeight"))).get(0));
        medDaysDayMinusWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("medDaysDayMinusWeight"))).get(0));
        medDaysDayPlusWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("medDaysDayPlusWeight"))).get(0));
        dayMinusDeviNumberWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("dayMinusDeviNumberWeight"))).get(0));
        dayPlusDeviNumberWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
                "ratings_weightage",
                new ArrayList<>(List.of("dayPlusDeviNumberWeight"))).get(0));
//        priceWeight = Float.parseFloat(systemConfigurationService.findPropertiesByGroupAndName("ratings",
//                "ratings_weightage",
//                new ArrayList<>(List.of("priceWeight"))).get(0));
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
                Rating rating = getRatingBySupplier(supp, matCode);
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
    }

    private Rating getRatingBySupplier(Supplier supplier, String materialCode) {
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
        int totalDeliveries = deliveries.size();

        if (totalDeliveries < minDeliveries)
            return null;
        for (Delivery delivery : deliveries) {
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

        float ratingPercentageQty = medPercQtyMinusWeight * medPercQtyMinus +
                medPercQtyPlusWeight * medPercQtyPlus;//lower is better(0-inf)
        float ratingNrDeviationsQty = 1 - (qtyMinusDeviNumberWeight * qtyMinusDeviNumber / totalDeliveries +
                qtyPlusDeviNumberWeight * qtyPlusDeviNumber / totalDeliveries);//higher is better (1-0)

        float ratingPercentageDay = medDaysDayMinusWeight * medDaysDayMinus +
                medDaysDayPlusWeight * medDaysDayPlus;//lower is better(0-inf)
        float ratingNrDeviationsDay = 1 - (dayMinusDeviNumberWeight * dayMinusDeviNumber / totalDeliveries +
                dayPlusDeviNumberWeight * dayPlusDeviNumber / totalDeliveries);//higher is better (1-0)

        Rating rating = new Rating();
        rating.setQtyPercentageRating(ratingPercentageQty);
        rating.setQtyNrDevisRating(ratingNrDeviationsQty);
        rating.setDayPercentageRating(ratingPercentageDay);
        rating.setDayNrDevisRating(ratingNrDeviationsDay);
        rating.setTotalNumberDeliveries(totalDeliveries);
        rating.setCorrectDeliveriesPercentage((float) correctDeliveriesNr / totalDeliveries);
        rating.setSupplier(supplier);
        if (materialCode == null || materialCode.equals(""))
            rating.setMaterialCode("all");
        else
            rating.setMaterialCode(materialCode);
        return rating;
    }

    private void curveRating() {
        List<String> allMaterials = new ArrayList<>();
        List<Rating> ratings = ratingRepository.findAll();
        for (Rating rating : ratings) {
            if(!allMaterials.contains(rating.getMaterialCode()))
                allMaterials.add(rating.getMaterialCode());
        }

        for (String material : allMaterials) {
            Float maxQty = Float.MIN_VALUE;
            Float maxDay = Float.MIN_VALUE;
            for (Rating rating : ratings) {
                if (rating.getMaterialCode().equals(material)) {
                    if(rating.getQtyPercentageRating() > maxQty)
                        maxQty = rating.getQtyPercentageRating();
                    if(rating.getDayPercentageRating() > maxDay)
                        maxDay = rating.getDayPercentageRating();
                }
            }

            System.out.println("-------");
            System.out.println(material);
            System.out.println(maxQty);
            System.out.println(maxDay);
            for (Rating rating : ratings) {
                if (rating.getMaterialCode().equals(material)) {
                    rating.setQtyDeviationCurveRating(1-(rating.getQtyPercentageRating()/maxQty));
                    rating.setDayDeviationCurveRating(1-(rating.getDayPercentageRating()/maxDay));
                    ratingRepository.save(rating);
                }
            }
        }
    }

}
