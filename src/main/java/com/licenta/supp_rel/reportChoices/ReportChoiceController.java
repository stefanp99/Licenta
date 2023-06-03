package com.licenta.supp_rel.reportChoices;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reportChoices")
@RequiredArgsConstructor
public class ReportChoiceController {

    private final ReportChoiceService reportChoiceService;

    @GetMapping("byUser")
    public List<ReportChoiceWithoutUserDTO> getReportChoiceByUser(@RequestParam(value = "userId", required = false) String userId) {
        return reportChoiceService.findReportChoiceByUser(userId);
    }

    @PostMapping("addChoice")
    public ReportChoiceWithoutUserDTO addReportChoice(@RequestParam("userId") Integer userId,
                                        @RequestParam(value = "plantId", required = false) String plantId,
                                        @RequestParam(value = "supplierId", required = false) String supplierId,
                                        @RequestParam(value = "materialCode", required = false) String materialCode){
        return reportChoiceService.addReportChoice(userId, plantId, supplierId, materialCode);
    }

    @PutMapping("updateChoice")
    public ReportChoiceWithoutUserDTO updateReportChoice(@RequestParam("userId") Integer userId,
                                        @RequestParam(value = "plantId", required = false) String plantId,
                                        @RequestParam(value = "supplierId", required = false) String supplierId,
                                        @RequestParam(value = "materialCode", required = false) String materialCode){
        return reportChoiceService.updateReportChoice(userId, plantId, supplierId, materialCode);
    }

    @DeleteMapping("deleteChoice")
    public ReportChoiceWithoutUserDTO deleteReportChoice(@RequestParam("userId") Integer userId){
        return reportChoiceService.deleteReportChoice(userId);
    }
}
