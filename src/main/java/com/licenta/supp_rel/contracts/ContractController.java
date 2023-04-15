package com.licenta.supp_rel.contracts;

import com.licenta.supp_rel.plants.PlantRepository;
import com.licenta.supp_rel.suppliers.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;
    @Autowired
    ContractRepository contractRepository;
    @Autowired
    PlantRepository plantRepository;
    @Autowired
    SupplierRepository supplierRepository;

    @GetMapping("get-by-plantId-supplierId-materialCode")
    public List<Contract> getContractsByMaterialCodePlantId(@RequestParam(value = "plantId", required = false) String plantId,
                                                            @RequestParam(value = "supplierId", required = false) String supplierId,
                                                            @RequestParam(value = "materialCode", required = false) String materialCode) {
        List<Contract> returnedList;
        if (plantId == null || plantId.equals("") || plantId.equals("null") || plantId.equals("All"))
            plantId = "*";
        if (supplierId == null || supplierId.equals("") || supplierId.equals("null") || supplierId.equals("All"))
            supplierId = "*";
        if (materialCode == null || materialCode.equals("") || materialCode.equals("null") || materialCode.equals("All"))
            materialCode = "*";
        returnedList = contractService.findContractsByPlantIdSupplierIdMaterialCode(plantId, supplierId, materialCode);
        returnedList.sort(Comparator.comparing(o -> o.getSupplier().getId()));
        return returnedList;
    }

    @PostMapping("add")
    public Contract addContract(@RequestParam("plantId") String plantId,
                                  @RequestParam("supplierId") String supplierId,
                                  @RequestParam("materialCode") String materialCode,
                                  @RequestParam("pricePerUnit") Float pricePerUnit){
        Contract contract = new Contract();
        contract.setPlant(plantRepository.findById(plantId).orElse(null));
        contract.setSupplier(supplierRepository.findById(supplierId).orElse(null));
        contract.setMaterialCode(materialCode);
        contract.setPricePerUnit(pricePerUnit);
        contractRepository.save(contract);
        return contract;
    }

    @PutMapping("update")
    public Contract updateContract(@RequestParam("id") Integer id,
                                     @RequestParam("plantId") String plantId,
                                     @RequestParam("supplierId") String supplierId,
                                     @RequestParam("materialCode") String materialCode,
                                     @RequestParam("pricePerUnit") Float pricePerUnit){
        Contract contract = contractRepository.findById(id).orElse(null);
        if(contract!=null) {
            contract.setPlant(plantRepository.findById(plantId).orElse(null));
            contract.setSupplier(supplierRepository.findById(supplierId).orElse(null));
            contract.setMaterialCode(materialCode);
            contract.setPricePerUnit(pricePerUnit);
            contractRepository.save(contract);
        }
        return contract;
    }

    @DeleteMapping("delete")
    public Contract deleteContract(@RequestParam("id") Integer id){
        Contract contract = contractRepository.findById(id).orElse(null);
        if(contract!=null)
            contractRepository.delete(contract);
        return contract;
    }
}
