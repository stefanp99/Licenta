package com.licenta.supp_rel.ratings;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("by-supplier-material-plant-type")
    public List<Object> getRatingsBySupplierMaterialPlant(@RequestParam(value = "supplierId", required = false) String supplierId,
                                                                              @RequestParam(value = "materialCode", required = false) String materialCode,
                                                                              @RequestParam(value = "plantId", required = false) String plantId,
                                                                              @RequestParam(value = "ratingType", required = false) String ratingType,
                                                                              @RequestParam(value = "chart", required = false) String chart){
        if (ratingType == null || ratingType.equals("") || ratingType.equals("null") || ratingType.equals("All") || ratingType.equals("all"))
            ratingType = "*";
        if (plantId == null || plantId.equals("") || plantId.equals("null") || plantId.equals("All"))
            plantId = "*";
        if (supplierId == null || supplierId.equals("") || supplierId.equals("null") || supplierId.equals("All"))
            supplierId = "*";
        if (materialCode == null || materialCode.equals("") || materialCode.equals("null") || materialCode.equals("All"))
            materialCode = "*";
        return ratingService.findRatingsBySupplierMaterialPlant(supplierId, materialCode, plantId, ratingType, chart);
    }
}
