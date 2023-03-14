package com.licenta.supp_rel.suppliers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SupplierService {
    @Autowired
    SupplierRepository supplierRepository;
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
        return matchingSuppliers;
    }
}
