package com.licenta.supp_rel.ratings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licenta.supp_rel.charts.ChartsService;
import com.licenta.supp_rel.contracts.Contract;
import com.licenta.supp_rel.contracts.ContractService;
import com.licenta.supp_rel.deliveries.Delivery;
import com.licenta.supp_rel.deliveries.DeliveryService;
import com.licenta.supp_rel.deviations.Deviation;
import com.licenta.supp_rel.deviations.DeviationRepository;
import com.licenta.supp_rel.deviations.DeviationTypes;
import com.licenta.supp_rel.plants.Plant;
import com.licenta.supp_rel.plants.PlantRepository;
import com.licenta.supp_rel.suppliers.Supplier;
import com.licenta.supp_rel.suppliers.SupplierRepository;
import com.licenta.supp_rel.systemConfigurations.RatingsWeightageDTO;
import com.licenta.supp_rel.systemConfigurations.SystemConfiguration;
import com.licenta.supp_rel.systemConfigurations.SystemConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class RatingService {
    @Autowired
    SystemConfigurationRepository systemConfigurationRepository;
    @Autowired
    DeliveryService deliveryService;
    @Autowired
    ContractService contractService;
    @Autowired
    PlantRepository plantRepository;
    @Autowired
    SupplierRepository supplierRepository;
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    DeviationRepository deviationRepository;
    @Autowired
    ChartsService chartsService;

    List<Rating> createRatings(String supplierId, String materialCode, String plantId) {
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
                        List<String> plantIds = new ArrayList<>();
                        if (plantId == null || plantId.isEmpty()) {
                            List<Plant> plants = contractService.findPlantsBySupplierAndMaterialCode(supp, matCode);
                            for (Plant plant : plants)
                                if (!plantIds.contains(plant.getId()))
                                    plantIds.add(plant.getId());
                            plantIds.add(null);
                        } else
                            plantIds.add(plantId);
                        for (String pId : plantIds) {
                            Rating rating = getRatingBySupplierAndMaterial(supp, matCode, pId, ratingsWeightageDTO);
                            if (rating != null && !ratings.contains(rating))
                                ratings.add(rating);
                        }
                    }
                }
                curveRating(ratings);
                ratingRepository.saveAll(ratings);
                return ratings;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private Rating getRatingBySupplierAndMaterial(Supplier supplier, String materialCode, String plantId, RatingsWeightageDTO ratingsWeightageDTO) {
        float medPercQtyMinus = 0F;
        float medPercQtyPlus = 0F;

        float medDaysDayMinus = 0F;
        float medDaysDayPlus = 0F;

        int qtyMinusDeviNumber = 0;
        int qtyPlusDeviNumber = 0;
        int dayMinusDeviNumber = 0;
        int dayPlusDeviNumber = 0;

        int correctDeliveriesNr = 0;

        List<Delivery> deliveries = deliveryService.findAllBySupplierAndMaterialCodeAndPlantIdAndStatus(supplier, materialCode, plantId, "delivered");
        int totalDeliveriesNumber = deliveries.size();

        if (totalDeliveriesNumber < ratingsWeightageDTO.getMinDeliveries())
            return null;

        float totalTimeDifferenceInHours = 0F;
        float totalLeadTimeInHours = 0F;
        for (Delivery delivery : deliveries) {
            float timeDifferenceInHours = Math.abs((delivery.getDeliveryDate().getTime() - delivery.getDispatchDate().getTime()) / (float) (60 * 60 * 1000));
            totalTimeDifferenceInHours += timeDifferenceInHours;
            float leadTimeInHours = Math.abs((delivery.getDispatchDate().getTime() - delivery.getAddDeliveryDate().getTime()) / (float) (60 * 60 * 1000));
            totalLeadTimeInHours += leadTimeInHours;
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
        rating.setAverageNumberOfHoursToDeliver(totalTimeDifferenceInHours / deliveries.size());
        rating.setAverageLeadTimeInHours(totalLeadTimeInHours / deliveries.size());
        List<Contract> contracts;
        if (materialCode == null || materialCode.equals("")) {
            rating.setMaterialCode("all");
            if (plantId == null)
                rating.setPlantId("all");
            else {
                rating.setDistanceToPlant(contractService.calculateDistanceBySupplierAndPlant(supplier,
                        Objects.requireNonNull(plantRepository.findById(plantId).orElse(null))));
                rating.setPlantId(plantId);
            }
        } else {
            Float averagePrice;
            rating.setMaterialCode(materialCode);
            if (plantId == null) {
                averagePrice = contractService.getAveragePriceByMaterialCode(materialCode);
                contracts = contractService.findContractsBySupplierAndMaterialCode(supplier, materialCode);
                rating.setPlantId("all");
                //rating.setDistanceToPlant(contractService.averageDistanceBySupplierAndMaterialsAndPlants(rating.getSupplier(), List.of(rating.getMaterialCode()), plants));
            } else {
                averagePrice = contractService.getAveragePriceByMaterialCodeAndPlant(materialCode,
                        plantRepository.findById(plantId).orElse(null));
                contracts = contractService.findContractsBySupplierAndMaterialCodeAndPlant(supplier, materialCode,
                        plantRepository.findById(plantId).orElse(null));
                rating.setDistanceToPlant(contractService.calculateDistanceBySupplierAndPlant(supplier,
                        Objects.requireNonNull(plantRepository.findById(plantId).orElse(null))));
                rating.setPlantId(plantId);
            }
            Contract contract;
            if (contracts.size() > 0) {
                contract = contracts.get(0);
                rating.setPriceDeviationPercentage((contract.getPricePerUnit() - averagePrice) * 100 / averagePrice);
            }
        }

        return rating;
    }

    private void curveRating(List<Rating> ratings) {
        List<String> allMaterials = new ArrayList<>();
        List<String> allPlants = new ArrayList<>();
        for (Rating rating : ratings) {
            if (!allMaterials.contains(rating.getMaterialCode()))
                allMaterials.add(rating.getMaterialCode());
            if (!allPlants.contains(rating.getPlantId()))
                allPlants.add(rating.getPlantId());
        }

        for (String material : allMaterials) {
            for (String plantId : allPlants) {
                Float maxQty = Float.MIN_VALUE;
                Float maxDay = Float.MIN_VALUE;
                for (Rating rating : ratings) {
                    if (rating.getMaterialCode().equals(material) && rating.getPlantId().equals(plantId)) {
                        if (rating.getQtyPercentageRating() > maxQty)
                            maxQty = rating.getQtyPercentageRating();
                        if (rating.getDayPercentageRating() > maxDay)
                            maxDay = rating.getDayPercentageRating();
                    }
                }

                for (Rating rating : ratings) {
                    if (rating.getMaterialCode().equals(material) && rating.getPlantId().equals(plantId)) {
                        rating.setQtyDeviationCurveRating(1 - (rating.getQtyPercentageRating() / maxQty));
                        rating.setDayDeviationCurveRating(1 - (rating.getDayPercentageRating() / maxDay));
                    }
                }
            }
        }
    }

    public List<Rating> findRatingsBySupplierMaterialCodePlantId(Supplier supplier, String materialCode, String plantId) {
        if (materialCode == null) {
            if (plantId == null)
                return ratingRepository.findBySupplier(supplier);
            else
                return ratingRepository.findBySupplierAndPlantId(supplier, plantId);
        } else {
            if (plantId == null)
                return ratingRepository.findBySupplierAndMaterialCode(supplier, materialCode);
            else
                return ratingRepository.findBySupplierAndMaterialCodeAndPlantId(supplier, materialCode, plantId);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T findRatingsBySupplierMaterialPlant(String supplierId, String materialCode, String plantId, String ratingType, String chart) {
        List<Rating> ratings;
        switch (ratingType) {
            case "global" -> ratings = findGlobalRatings(supplierId);
            case "material" -> ratings = findMaterialRatings(supplierId, materialCode);
            case "plant" -> ratings = findPlantRatings(supplierId, plantId);
            case "specific" -> ratings = findSpecificRatings(supplierId, materialCode, plantId);
            default -> ratings = null;
        }
        if (ratings == null)
            return null;
        switch (chart) {
            case "curveCorrectPerc" -> {
                return (T) chartsService.getCurveRatingsAndCorrectDeliveriesPercentage(ratings);
            }
            case "totalNrDeliveries" -> {
                return (T) chartsService.getTotalNumberOfDeliveries(ratings);
            }
            case "priceDeviation" -> {
                return (T) chartsService.getPriceDeviations(ratings);
            }
            case "distanceToPlant" -> {
                return (T) chartsService.getDistanceToPlant(ratings);
            }
            case "averageHours" -> {
                return (T) chartsService.getAllAverageHours(ratings);
            }
            case "table" -> {
                return (T) ratings;
            }
            default -> {
                return null;
            }
        }
    }

    private List<Rating> findSpecificRatings(String supplierIdInput, String materialCodeInput, String plantIdInput) {
        List<Rating> allRatings = ratingRepository.findAll();
        List<Rating> matchingRatings = new ArrayList<>();

        List<String> plantIds = Arrays.asList(plantIdInput.split(","));
        List<String> supplierIds = Arrays.asList(supplierIdInput.split(","));
        List<String> materialCodes = Arrays.asList(materialCodeInput.split(","));

        for (Rating rating : allRatings) {
            if ((plantIds.contains(rating.getPlantId()) || plantIds.contains("*")) &&
                    (supplierIds.contains(rating.getSupplier().getId()) || supplierIds.contains("*")) &&
                    (materialCodes.contains(rating.getMaterialCode()) || materialCodes.contains("*")) &&
                    (!rating.getMaterialCode().equals("all") && !rating.getPlantId().equals("all"))) {
                matchingRatings.add(rating);
            }
        }
        return matchingRatings;
    }

    private List<Rating> findPlantRatings(String supplierIdInput, String plantIdInput) {
        List<Rating> allRatings = ratingRepository.findAll();
        List<Rating> matchingRatings = new ArrayList<>();

        List<String> plantIds = Arrays.asList(plantIdInput.split(","));
        List<String> supplierIds = Arrays.asList(supplierIdInput.split(","));

        for (Rating rating : allRatings) {
            if ((plantIds.contains(rating.getPlantId()) || plantIds.contains("*")) &&
                    (supplierIds.contains(rating.getSupplier().getId()) || supplierIds.contains("*")) &&
                    (rating.getMaterialCode().equals("all") && !rating.getPlantId().equals("all"))) {
                matchingRatings.add(rating);
            }
        }
        return matchingRatings;
    }

    private List<Rating> findMaterialRatings(String supplierIdInput, String materialCodeInput) {
        List<Rating> allRatings = ratingRepository.findAll();
        List<Rating> matchingRatings = new ArrayList<>();

        List<String> supplierIds = Arrays.asList(supplierIdInput.split(","));
        List<String> materialCodes = Arrays.asList(materialCodeInput.split(","));

        for (Rating rating : allRatings) {
            if ((supplierIds.contains(rating.getSupplier().getId()) || supplierIds.contains("*")) &&
                    (materialCodes.contains(rating.getMaterialCode()) || materialCodes.contains("*")) &&
                    (!rating.getMaterialCode().equals("all") && rating.getPlantId().equals("all"))) {
                matchingRatings.add(rating);
            }
        }
        return matchingRatings;
    }

    private List<Rating> findGlobalRatings(String supplierIdInput) {
        List<Rating> allRatings = ratingRepository.findAll();
        List<Rating> matchingRatings = new ArrayList<>();

        List<String> supplierIds = Arrays.asList(supplierIdInput.split(","));

        for (Rating rating : allRatings) {
            if ((supplierIds.contains(rating.getSupplier().getId()) || supplierIds.contains("*")) &&
                    rating.getMaterialCode().equals("all") && rating.getPlantId().equals("all")) {
                matchingRatings.add(rating);
            }
        }
        return matchingRatings;
    }
}
