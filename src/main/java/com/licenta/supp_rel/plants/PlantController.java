package com.licenta.supp_rel.plants;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("plants")
@RequiredArgsConstructor
public class PlantController {
    private final PlantService plantService;
    @Autowired
    PlantRepository plantRepository;
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

    @GetMapping("by-id")
    public List<Plant> getPlantsById(@RequestParam(value = "plantId", required = false) String plantId){
        if(plantId == null || plantId.equals(""))
            return plantRepository.findAll();
        else{
            List<Plant> plants = new ArrayList<>();
            String[] plantIds = plantId.split(",");
            for(String pId: plantIds) {
                plantRepository.findById(pId).ifPresent(plants::add);
            }
            return plants;
        }
    }

}
