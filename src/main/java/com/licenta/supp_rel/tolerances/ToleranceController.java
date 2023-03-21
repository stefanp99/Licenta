package com.licenta.supp_rel.tolerances;

import com.licenta.supp_rel.suppliers.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tolerances")
@RequiredArgsConstructor
public class ToleranceController {
    private final ToleranceService toleranceService;
    @Autowired
    ToleranceRepository toleranceRepository;
    @Autowired
    SupplierRepository supplierRepository;
    @GetMapping("get-by-plantId-supplierId-materialCode")
    public List<Tolerance> getTolerancesByPlantIdSupplierIdMaterialCode(@RequestParam(value = "plantId", required = false) String plantId,
                                                                        @RequestParam(value = "supplierId", required = false) String supplierId,
                                                                        @RequestParam(value = "materialCode", required = false) String materialCode){
        if(plantId == null || plantId.equals("") || plantId.equals("null") || plantId.equals("All"))
            plantId = "*";
        if(supplierId == null || supplierId.equals("") || supplierId.equals("null") || supplierId.equals("All"))
            supplierId = "*";
        if(materialCode == null || materialCode.equals("") || materialCode.equals("null") || materialCode.equals("All"))
            materialCode = "*";
        return toleranceService.findTolerancesByPlantIdSupplierIdMaterialCode(plantId, supplierId, materialCode);
    }

    @PostMapping("add")
    public Tolerance addTolerance(@RequestParam("plantId") String plantId,
                                  @RequestParam("supplierId") String supplierId,
                                  @RequestParam("materialCode") String materialCode,
                                  @RequestParam("qtyUpperLimit") Float qtyUpperLimit,
                                  @RequestParam("qtyLowerLimit") Float qtyLowerLimit,
                                  @RequestParam("dayUpperLimit") Integer dayUpperLimit,
                                  @RequestParam("dayLowerLimit") Integer dayLowerLimit){
        Tolerance tolerance = new Tolerance();
        tolerance.setPlantId(plantId);
        tolerance.setSupplier(supplierRepository.findById(supplierId).orElse(null));
        tolerance.setMaterialCode(materialCode);
        tolerance.setQtyUpperLimit(qtyUpperLimit);
        tolerance.setQtyLowerLimit(qtyLowerLimit);
        tolerance.setDayUpperLimit(dayUpperLimit);
        tolerance.setDayLowerLimit(dayLowerLimit);
        toleranceRepository.save(tolerance);
        return tolerance;
    }

    @PutMapping("update")
    public Tolerance updateTolerance(@RequestParam("id") Integer id,
                                     @RequestParam("plantId") String plantId,
                                     @RequestParam("supplierId") String supplierId,
                                     @RequestParam("materialCode") String materialCode,
                                     @RequestParam("qtyUpperLimit") Float qtyUpperLimit,
                                     @RequestParam("qtyLowerLimit") Float qtyLowerLimit,
                                     @RequestParam("dayUpperLimit") Integer dayUpperLimit,
                                     @RequestParam("dayLowerLimit") Integer dayLowerLimit){
        Tolerance tolerance = toleranceRepository.findById(id).orElse(null);
        if(tolerance!=null) {
            tolerance.setPlantId(plantId);
            tolerance.setSupplier(supplierRepository.findById(supplierId).orElse(null));
            tolerance.setMaterialCode(materialCode);
            tolerance.setQtyUpperLimit(qtyUpperLimit);
            tolerance.setQtyLowerLimit(qtyLowerLimit);
            tolerance.setDayUpperLimit(dayUpperLimit);
            tolerance.setDayLowerLimit(dayLowerLimit);
            toleranceRepository.save(tolerance);
        }
        return tolerance;
    }

    @DeleteMapping("delete")
    public Tolerance deleteTolerance(@RequestParam("id") Integer id){
        Tolerance tolerance = toleranceRepository.findById(id).orElse(null);
        if(tolerance!=null)
            toleranceRepository.delete(tolerance);
        return tolerance;
    }

}
