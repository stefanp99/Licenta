package com.licenta.supp_rel.suppliers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;
    @GetMapping("get-suppliers-by-city-country")
    List<Supplier> getSuppliersByCityCountry(@RequestParam(value = "city", required = false) String city,
                                             @RequestParam(value = "country", required = false) String country){
        if(city == null || city.equals("")) {
            city = "*";
        }
        if(country == null || country.equals("")) {
            country = "*";
        }
        return supplierService.findSuppliersByCityCountry(city, country);
    }
}
