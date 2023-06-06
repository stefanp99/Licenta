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
        //find configurations
        List<SystemConfiguration> systemConfigurations = systemConfigurationRepository.findAllByConfigGroupAndConfigName("ratings", "ratings_weightage");
        SystemConfiguration systemConfiguration = null;
        RatingsWeightageDTO ratingsWeightageDTO;
        if (systemConfigurations.size() > 0)
            systemConfiguration = systemConfigurations.get(0);
        if (systemConfiguration != null) {
            try {
                //build WeightageDTO in order to use it later for weightages
                ratingsWeightageDTO = objectMapper.readValue(systemConfiguration.getConfigValues(), RatingsWeightageDTO.class);
                //get all needed suppliers
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
                    //for each supplier get its material codes from contracts
                    List<String> materialCodes = new ArrayList<>();
                    if (materialCode == null || materialCode.isEmpty()) {
                        materialCodes = contractService.findMaterialCodesBySupplier(supp);
                        materialCodes.add(null);
                    } else
                        materialCodes.add(materialCode);

                    for (String matCode : materialCodes) {
                        //for each supplier and material code get their plants from contracts
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
                            //call method for getting the ratings by supplier, material, plant and weightages
                            Rating rating = getRatingBySupplierAndMaterial(supp, matCode, pId, ratingsWeightageDTO);
                            if (rating != null && !ratings.contains(rating))
                                ratings.add(rating);
                        }
                    }
                }
                //call method for calculating the curve ratings
                curveRating(ratings);
                ratingRepository.deleteAll();
                ratingRepository.saveAll(ratings);
                return ratings;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private Rating getRatingBySupplierAndMaterial(Supplier supplier, String materialCode, String plantId, RatingsWeightageDTO ratingsWeightageDTO) {
        float medPercQtyMinus = 0F;//medium percentage of qty minus deviations
        float medPercQtyPlus = 0F;//medium percentage of qty plus deviations

        float medDaysDayMinus = 0F;//medium days of day minus deviations
        float medDaysDayPlus = 0F;//medium days of day plus deviations

        int qtyMinusDeviNumber = 0;//nr of qty minus deviations
        int qtyPlusDeviNumber = 0;//nr of qty plus deviations
        int dayMinusDeviNumber = 0;//nr of day minus deviations
        int dayPlusDeviNumber = 0;//nr of day plus deviations

        int correctDeliveriesNr = 0;//nr of correct deliveries(no deviations for that delivery)

        List<Delivery> deliveries = deliveryService.findAllBySupplierAndMaterialCodeAndPlantIdAndStatus(supplier, materialCode, plantId, "delivered");
        int totalDeliveriesNumber = deliveries.size();//total nr of deliveries

        if (totalDeliveriesNumber < ratingsWeightageDTO.getMinDeliveries())//must be higher than minDeliveries
            return null;

        float totalTimeDifferenceInHours = 0F;
        float totalLeadTimeInHours = 0F;
        for (Delivery delivery : deliveries) {
            //time it takes for the deliveries to be delivered
            float timeDifferenceInHours = Math.abs((delivery.getDeliveryDate().getTime() - delivery.getDispatchDate().getTime()) / (float) (60 * 60 * 1000));
            totalTimeDifferenceInHours += timeDifferenceInHours;
            //time it takes for the deliveries to be dispatched(aka lead time)
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

        //getting the rating by taking in consideration the percentage of qty deviations
        float ratingPercentageQty = ratingsWeightageDTO.getMedPercQtyMinusWeight() * medPercQtyMinus +
                ratingsWeightageDTO.getMedPercQtyPlusWeight() * medPercQtyPlus;//lower is better(0-inf)
        //getting the rating by taking in consideration the number of qty deviations
        float ratingNrDeviationsQty = 1 - (ratingsWeightageDTO.getQtyMinusDeviNumberWeight() * qtyMinusDeviNumber / totalDeliveriesNumber +
                ratingsWeightageDTO.getQtyPlusDeviNumberWeight() * qtyPlusDeviNumber / totalDeliveriesNumber);//higher is better (1-0)

        //same as above but for day deviations
        float ratingPercentageDay = ratingsWeightageDTO.getMedDaysDayMinusWeight() * medDaysDayMinus +
                ratingsWeightageDTO.getMedDaysDayPlusWeight() * medDaysDayPlus;//lower is better(0-inf)
        float ratingNrDeviationsDay = 1 - (ratingsWeightageDTO.getDayMinusDeviNumberWeight() * dayMinusDeviNumber / totalDeliveriesNumber +
                ratingsWeightageDTO.getDayPlusDeviNumberWeight() * dayPlusDeviNumber / totalDeliveriesNumber);//higher is better (1-0)

        //creating a new rating
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
            if (plantId == null)//plant rating
                rating.setPlantId("all");
            else {
                rating.setDistanceToPlant(contractService.calculateDistanceBySupplierAndPlant(supplier,
                        Objects.requireNonNull(plantRepository.findById(plantId).orElse(null))));//distance from the supplier to the plant
                rating.setPlantId(plantId);
            }
        } else {
            Float averagePrice;
            rating.setMaterialCode(materialCode);
            if (plantId == null) {
                averagePrice = contractService.getAveragePriceByMaterialCode(materialCode);//average price from all suppliers for a specific material
                contracts = contractService.findContractsBySupplierAndMaterialCode(supplier, materialCode);
                rating.setPlantId("all");
                //rating.setDistanceToPlant(contractService.averageDistanceBySupplierAndMaterialsAndPlants(rating.getSupplier(), List.of(rating.getMaterialCode()), plants));
            } else {
                averagePrice = contractService.getAveragePriceByMaterialCodeAndPlant(materialCode,
                        plantRepository.findById(plantId).orElse(null));//average price from all suppliers for a specific material and plant
                contracts = contractService.findContractsBySupplierAndMaterialCodeAndPlant(supplier, materialCode,
                        plantRepository.findById(plantId).orElse(null));
                rating.setDistanceToPlant(contractService.calculateDistanceBySupplierAndPlant(supplier,
                        Objects.requireNonNull(plantRepository.findById(plantId).orElse(null))));//distance from supplier to plant
                rating.setPlantId(plantId);
            }
            Contract contract;
            if (contracts.size() > 0) {
                contract = contracts.get(0);
                //percentage of difference between this supplier's price per unit and average supplier's price per unit
                rating.setPriceDeviationPercentage((contract.getPricePerUnit() - averagePrice) * 100 / averagePrice);
            }
        }

        return rating;
    }

    /**
     * method to curve rate all ratings by qty and day percentage deviations
     * @param ratings ratings to be taken in consideration
     */
    private void curveRating(List<Rating> ratings) {
        List<String> allMaterials = new ArrayList<>();
        List<String> allPlants = new ArrayList<>();
        // all materials and all plants, no duplicates. should have used a set.
        for (Rating rating : ratings) {
            if (!allMaterials.contains(rating.getMaterialCode()))
                allMaterials.add(rating.getMaterialCode());
            if (!allPlants.contains(rating.getPlantId()))
                allPlants.add(rating.getPlantId());
        }

        //getting the maximum percentage of deviations by qty and day
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
                        //formula: curveRating = 1-rating/maxRating
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
            case "totalNrDeliveriesPieChart" -> {
                return (T) chartsService.getTotalNrOfDeliveriesPieChart(ratings);
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
