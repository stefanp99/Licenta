package com.licenta.supp_rel.contracts;

import com.licenta.supp_rel.plants.Plant;
import com.licenta.supp_rel.suppliers.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public List<Plant> findPlantsBySupplier(Supplier supplier) {
        List<Plant> returnedList = new ArrayList<>();
        List<Contract> contracts = contractRepository.findAllBySupplier(supplier);
        for (Contract contract : contracts)
            returnedList.add(contract.getPlant());
        return returnedList;
    }

    public Float getAveragePriceByMaterialCode(String materialCode) {
        List<Contract> contracts = contractRepository.findAllByMaterialCode(materialCode);
        Float totalPrice = 0F;
        for (Contract contract : contracts)
            totalPrice += contract.getPricePerUnit();
        return totalPrice / contracts.size();
    }

    public Float getAveragePriceByMaterialCodeAndPlant(String materialCode, Plant plant) {
        List<Contract> contracts = contractRepository.findAllByMaterialCodeAndPlant(materialCode, plant);
        Float totalPrice = 0F;
        for (Contract contract : contracts)
            totalPrice += contract.getPricePerUnit();
        return totalPrice / contracts.size();
    }

    public List<Contract> findContractsBySupplierAndMaterialCode(Supplier supplier, String materialCode) {
        return contractRepository.findAllBySupplierAndMaterialCode(supplier, materialCode);
    }

    public List<Contract> findContractsBySupplierAndPlant(Supplier supplier, Plant plant) {
        return contractRepository.findAllBySupplierAndPlant(supplier, plant);
    }

    public List<Contract> findContractsBySupplierAndMaterialCodeAndPlant(Supplier supplier, String materialCode, Plant plant) {
        return contractRepository.findAllBySupplierAndMaterialCodeAndPlant(supplier, materialCode, plant);
    }

    public List<Contract> findContractsBySupplierAndMaterialCodeAndPlantNoRepeat(Supplier supplier, String materialCode, Plant plant) {
        List<Contract> contracts;
        if(materialCode == null) {
            if (plant == null)
                contracts = contractRepository.findAllBySupplier(supplier);
            else
                contracts = contractRepository.findAllBySupplierAndPlant(supplier, plant);
        }
        else{
            if(plant == null)
                contracts = contractRepository.findAllBySupplierAndMaterialCode(supplier, materialCode);
            else
                contracts = contractRepository.findAllBySupplierAndMaterialCodeAndPlant(supplier, materialCode, plant);
        }
        Set<Contract> set = new HashSet<>(contracts);
        contracts.clear();
        contracts.addAll(set);
        return contracts;
    }

    public Float calculateDistanceBySupplierAndPlant(Supplier supplier, Plant plant){
        int radius = 6371; // Radius of the earth in km
        Float latSupplier = supplier.getCityLatitude();
        Float lonSupplier = supplier.getCityLongitude();

        Float latPlant = plant.getCityLatitude();
        Float lonPlant = plant.getCityLongitude();

        float latDistance = (float) Math.toRadians(latSupplier-latPlant);
        float lonDistance = (float) Math.toRadians(lonSupplier-lonPlant);
        float a = (float) (Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                        Math.cos(Math.toRadians(latSupplier)) * Math.cos(Math.toRadians(latPlant)) *
                                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
        return radius * c;
    }

    public Float averageDistanceBySupplierAndMaterialsAndPlants(Supplier supplier, List<String> materialCodes, List<Plant> plants){
        List<Contract> contracts = new ArrayList<>();
        float totalDistance = 0F;
        for(String materialCode: materialCodes) {
            for(Plant plant: plants)
                contracts.addAll(contractRepository.findAllBySupplierAndMaterialCodeAndPlant(supplier, materialCode, plant));
        }
        for (Contract contract : contracts)
            totalDistance += calculateDistanceBySupplierAndPlant(contract.getSupplier(), contract.getPlant());
        if(contracts.size() > 0)
            return totalDistance/contracts.size();
        return null;
    }

    public List<Plant> findPlantsBySupplierAndMaterialCode(Supplier supp, String matCode) {
        List<Plant> plants = new ArrayList<>();
        List<Contract> contracts;
        if(matCode == null)
            contracts = contractRepository.findAllBySupplier(supp);
        else
            contracts = contractRepository.findAllBySupplierAndMaterialCode(supp, matCode);
        Set<Contract> set = new HashSet<>(contracts);
        contracts.clear();
        contracts.addAll(set);
        for (Contract contract : contracts)
            plants.add(contract.getPlant());
        return plants;
    }
}
