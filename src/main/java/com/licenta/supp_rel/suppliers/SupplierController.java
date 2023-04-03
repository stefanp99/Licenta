package com.licenta.supp_rel.suppliers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;
    @Autowired
    SupplierRepository supplierRepository;

    @GetMapping("get-suppliers-by-city-country")
    public List<Supplier> getSuppliersByCityCountry(@RequestParam(value = "city", required = false) String city,
                                                    @RequestParam(value = "country", required = false) String country) {
        if (city == null || city.equals("")) {
            city = "*";
        }
        if (country == null || country.equals("")) {
            country = "*";
        }
        return supplierService.findSuppliersByCityCountry(city, country);
    }

    @GetMapping("tooltips")
    public List<SupplierTooltipDTO> getSupplierTooltipDTO(@RequestParam(value = "supplierId", required = false) String supplierId) {
        if (supplierId == null) {
            List<SupplierTooltipDTO> supplierTooltipDTOs = new ArrayList<>();
            List<Supplier> suppliers = supplierRepository.findAll();
            for(Supplier supplier: suppliers)
                supplierTooltipDTOs.add(supplierService.getSupplierTooltipDTO(supplier.getId()));
            return supplierTooltipDTOs;
        } else
            return List.of(supplierService.getSupplierTooltipDTO(supplierId));
    }
}
