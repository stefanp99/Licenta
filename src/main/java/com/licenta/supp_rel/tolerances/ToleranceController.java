package com.licenta.supp_rel.tolerances;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("tolerances")
@RequiredArgsConstructor
public class ToleranceController {
    private final ToleranceService toleranceService;
    @GetMapping("get-by-plantId-supplierId-materialCode")
    public List<Tolerance> getTolerancesByPlantIdSupplierIdMaterialCode(@RequestParam(value = "plantId", required = false) String plantId,
                                                                        @RequestParam(value = "supplierId", required = false) String supplierId,
                                                                        @RequestParam(value = "materialCode", required = false) String materialCode){
        if(plantId == null || plantId.equals(""))
            plantId = "*";
        if(supplierId == null || supplierId.equals(""))
            supplierId = "*";
        if(materialCode == null || materialCode.equals(""))
            materialCode = "*";
        return toleranceService.findTolerancesByPlantIdSupplierIdMaterialCode(plantId, supplierId, materialCode);
    }

}
