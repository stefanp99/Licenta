package com.licenta.supp_rel.suppliers;

import com.licenta.supp_rel.ratings.Rating;
import com.licenta.supp_rel.ratings.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class SupplierService {
    @Autowired
    SupplierRepository supplierRepository;
    @Autowired
    RatingService ratingService;
    public List<Supplier> findSuppliersByCityCountry(String cityInput, String countryInput) {
        List<Supplier> allSuppliers = supplierRepository.findAll();
        List<Supplier> matchingSuppliers = new ArrayList<>();

        List<String> cities = Arrays.asList(cityInput.split(","));
        List<String> countries = Arrays.asList(countryInput.split(","));

        for (Supplier supplier: allSuppliers) {
            // Check if the supplier matches any of the specified cities and countries
            if ((cities.contains("*") || cities.contains(supplier.getCity())) &&
                    (countries.contains("*") || countries.contains(supplier.getCountry()))) {
                // Add the matching Supplier to the list
                matchingSuppliers.add(supplier);
            }
        }
        matchingSuppliers.sort(Comparator.comparing(Supplier::getId));
        return matchingSuppliers;
    }

    public SupplierTooltipDTO getSupplierTooltipDTO(String supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId).orElse(null);
        if(supplier == null)
            return null;
        SupplierTooltipDTO supplierTooltipDTO = new SupplierTooltipDTO();
        supplierTooltipDTO.setId(supplier.getId());
        supplierTooltipDTO.setName(supplier.getName());
        supplierTooltipDTO.setCityCountry(supplier.getCityCountry());
        supplierTooltipDTO.setCityLatitude(supplier.getCityLatitude());
        supplierTooltipDTO.setCityLongitude(supplier.getCityLongitude());
        List<Rating> ratings = ratingService.findRatingsBySupplierMaterialCodePlantId(supplier, "all", "all");
        if(ratings != null && ratings.size() > 0){
            Rating rating = ratings.get(0);
            supplierTooltipDTO.setTotalNumberDeliveries(rating.getTotalNumberDeliveries());
            supplierTooltipDTO.setCorrectDeliveriesPercentage(rating.getCorrectDeliveriesPercentage());
            supplierTooltipDTO.setQtyDeviationCurveRating(rating.getQtyDeviationCurveRating());
            supplierTooltipDTO.setDayDeviationCurveRating(rating.getDayDeviationCurveRating());
            supplierTooltipDTO.setAverageNumberOfHoursToDeliver(rating.getAverageNumberOfHoursToDeliver());
            supplierTooltipDTO.setAverageLeadTimeInHours(rating.getAverageLeadTimeInHours());
        }
        return supplierTooltipDTO;
    }

    public List<String> findAllSupplierIds(){
        List<String> supplierIds = new ArrayList<>();
        supplierRepository.findAll().forEach(supplier -> supplierIds.add(supplier.getId()));
        return supplierIds;
    }
}
