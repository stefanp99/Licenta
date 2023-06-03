package com.licenta.supp_rel.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("daily")
    public List<DailyReportDTO> getDailyReport(@RequestParam("userId") Integer userId,
                                               @RequestParam(value = "date", required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd") Date date){
        return reportService.createDailyReport(userId, date);
    }
}
