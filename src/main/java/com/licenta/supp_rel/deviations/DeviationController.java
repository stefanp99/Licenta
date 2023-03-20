package com.licenta.supp_rel.deviations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(path = "deviations")
@RequiredArgsConstructor
public class DeviationController {
    @Autowired
    DeviationRepository deviationRepository;
    @GetMapping("by-type")
    public List<Deviation> getDeviationByType(@RequestParam(value = "type", required = false) String type){
        try{
            List<String> types = new ArrayList<>(Arrays.asList(type.split(",")));
            List<Deviation> returnedDeviations = new ArrayList<>();
            for(String t: types)
                returnedDeviations.addAll(deviationRepository.findByType(DeviationTypes.valueOf(t)));
            return returnedDeviations;
        }
        catch (IllegalArgumentException | NullPointerException e) {
            return deviationRepository.findAll();
        }
    }
}
