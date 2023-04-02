package com.licenta.supp_rel.ratings;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;
    @PostMapping("calculateRatings")
    public List<Rating> calculateRatings(@RequestParam(value = "supplierId", required = false) String supplierId,
                                         @RequestParam(value = "materialCode", required = false) String materialCode,
                                         @RequestParam(value = "plantId", required = false) String plantId){
        return ratingService.createRatings(supplierId, materialCode, plantId);
    }
}
