package com.licenta.supp_rel.contracts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("contracts")
public class ContractController {
    @Autowired
    ContractRepository contractRepository;

    @GetMapping("by-materialCode-plantId")
    public List<Contract> getContractsByMaterialCodePlantId(@RequestParam("materialCode") String materialCode,
                                                            @RequestParam("plantId") String plantId){
        return contractRepository.findAllByMaterialCodeAndPlantId(materialCode, plantId);
    }
}
