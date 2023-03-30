package com.licenta.supp_rel.contracts;

import com.licenta.supp_rel.suppliers.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ContractService {
    @Autowired
    ContractRepository contractRepository;

    public List<Contract> findContractsByPlantIdSupplierIdMaterialCode(String plantIdInput, String supplierIdInput, String materialCodeInput) {
        List<Contract> allContracts = contractRepository.findAll();
        List<Contract> matchingContracts = new ArrayList<>();

        List<String> plantIds = Arrays.asList(plantIdInput.split(","));
        List<String> supplierIds = Arrays.asList(supplierIdInput.split(","));
        List<String> materialCodes = Arrays.asList(materialCodeInput.split(","));

        for (Contract contract : allContracts) {
            // Check if the contract matches any of the specified plantIds, supplierIds, and materialCodes
            if ((plantIds.contains(contract.getPlant().getId()) || plantIds.contains("*")) &&
                    (supplierIds.contains(contract.getSupplier().getId()) || supplierIds.contains("*")) &&
                    (materialCodes.contains(contract.getMaterialCode()) || materialCodes.contains("*") || contract.getMaterialCode().equals("%"))) {
                // Add the matching contract to the list
                matchingContracts.add(contract);
            }
        }
        return matchingContracts;
    }

    public List<String> findMaterialCodesBySupplier(Supplier supplier) {
        List<String> returnedList = new ArrayList<>();
        List<Contract> contracts = contractRepository.findAllBySupplier(supplier);
        for (Contract contract : contracts)
            returnedList.add(contract.getMaterialCode());
        return returnedList;
    }

    public Float getAveragePriceByMaterialCode(String materialCode) {
        List<Contract> contracts = contractRepository.findAllByMaterialCode(materialCode);
        Float totalPrice = 0F;
        for (Contract contract : contracts)
            totalPrice += contract.getPricePerUnit();
        return totalPrice / contracts.size();
    }

    public Contract findContractBySupplierAndMaterialCode(Supplier supplier, String materialCode) {
        List<Contract> contracts = contractRepository.findAllBySupplierAndMaterialCode(supplier, materialCode);
        if (contracts.size() > 0)
            return contracts.get(0);
        return null;
    }
}
