package com.licenta.supp_rel.tolerances;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ToleranceService {
    @Autowired
    ToleranceRepository toleranceRepository;
    public List<Tolerance> findTolerancesByPlantIdSupplierIdMaterialCode(String plantIdInput,
                                                                         String supplierIdInput,
                                                                         String materialCodeInput) {
        List<Tolerance> allTolerances = toleranceRepository.findAll();
        List<Tolerance> matchingTolerances = new ArrayList<>();

        List<String> plantIds = Arrays.asList(plantIdInput.split(","));
        List<String> supplierIds = Arrays.asList(supplierIdInput.split(","));
        List<String> materialCodes = Arrays.asList(materialCodeInput.split(","));

        for (Tolerance tolerance : allTolerances) {
            // Check if the tolerance matches any of the specified plantIds, supplierIds, and materialCodes
            if ((plantIds.contains(tolerance.getPlantId()) || plantIds.contains("*") || tolerance.getPlantId().equals("%")) &&
                    (supplierIds.contains(tolerance.getSupplierId()) || supplierIds.contains("*")) &&
                    (materialCodes.contains(tolerance.getMaterialCode()) || materialCodes.contains("*"))) {
                // Add the matching tolerance to the list
                matchingTolerances.add(tolerance);
            }
        }
        return matchingTolerances;
    }
}
