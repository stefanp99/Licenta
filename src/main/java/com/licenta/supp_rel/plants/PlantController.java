package com.licenta.supp_rel.plants;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("plants")
@RequiredArgsConstructor
public class PlantController {
    private final PlantService plantService;
    @GetMapping("plants-by-city-country-segment")
    public List<Plant> getPlantsByCityCountry(@RequestParam(value = "city", required = false) String city,
                                              @RequestParam(value = "country", required = false) String country,
                                              @RequestParam(value = "segment", required = false) String segment){
        if(city == null || city.equals("")) {
            city = "*";
        }
        if(country == null || country.equals("")) {
            country = "*";
        }
        if(segment == null || segment.equals("")) {
            segment = "*";
        }
        return plantService.findPlantsByCityCountryAndSegment(city, country, segment);
    }

}
