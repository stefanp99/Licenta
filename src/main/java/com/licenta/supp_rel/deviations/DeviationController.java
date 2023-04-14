package com.licenta.supp_rel.deviations;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "deviations")
@RequiredArgsConstructor
public class DeviationController {
    private final DeviationService deviationService;
    @GetMapping("by-type-supplier-material-plant")
    public List<Deviation> getDeviationByType(@RequestParam(value = "type", required = false) String type,
                                              @RequestParam(value = "plantId", required = false) String plantId,
                                              @RequestParam(value = "supplierId", required = false) String supplierId,
                                              @RequestParam(value = "materialCode", required = false) String materialCode){
        if (type == null || type.equals("") || type.equals("null") || type.equals("All") || type.equals("all"))
            type = "*";
        if (plantId == null || plantId.equals("") || plantId.equals("null") || plantId.equals("All"))
            plantId = "*";
        if (supplierId == null || supplierId.equals("") || supplierId.equals("null") || supplierId.equals("All"))
            supplierId = "*";
        if (materialCode == null || materialCode.equals("") || materialCode.equals("null") || materialCode.equals("All"))
            materialCode = "*";
        return deviationService.findDeviationsByTypeSupplierMaterialPlant(type, plantId, supplierId, materialCode);
    }
}
