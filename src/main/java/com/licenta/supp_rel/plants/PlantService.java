package com.licenta.supp_rel.plants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class PlantService {
    @Autowired
    PlantRepository plantRepository;

    public List<Plant> findPlantsByCityAndCountry(String cityInput, String countryInput) {
        List<Plant> allPlants = plantRepository.findAll();
        List<Plant> matchingPlants = new ArrayList<>();

        List<String> cities = Arrays.asList(cityInput.split(","));
        List<String> countries = Arrays.asList(countryInput.split(","));

        for (Plant plant: allPlants) {
            // Check if the plant matches any of the specified cities and countries
            if ((cities.contains("*") || cities.contains(plant.getCity())) &&
                    (countries.contains("*") || countries.contains(plant.getCountry()))) {
                // Add the matching plant to the list
                matchingPlants.add(plant);
            }
        }
        return matchingPlants;
    }

    public List<Plant> findPlantsByCityCountryAndSegment(String city, String country, String segmentInput) {
        List<Plant> plantsByCityCountry = findPlantsByCityAndCountry(city, country);
        List<Plant> plantsBySegment = new ArrayList<>();
        if(segmentInput.equals("*"))
            plantsBySegment = plantRepository.findAll();
        else {
            List<String> segmentList = new ArrayList<>(Arrays.asList(segmentInput.split(",")));
            for (String segment : segmentList) {
                plantsBySegment.addAll(plantRepository.findBySegment(segment));
            }
        }
        List<Plant> commonPlants = new ArrayList<>(plantsByCityCountry);
        commonPlants.retainAll(plantsBySegment);
        commonPlants.sort(Comparator.comparing(Plant::getId));
        return commonPlants;
    }

    public List<String> findAllPlantIds() {
        List<Plant> allPlants = plantRepository.findAll();
        List<String> allPlantIds = new ArrayList<>();
        for(Plant plant: allPlants)
            if(!allPlantIds.contains(plant.getId()))
                allPlantIds.add(plant.getId());
        return allPlantIds;
    }
}
