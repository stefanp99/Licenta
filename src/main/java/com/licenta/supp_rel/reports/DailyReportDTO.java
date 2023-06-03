package com.licenta.supp_rel.reports;

import com.licenta.supp_rel.plants.Plant;
import com.licenta.supp_rel.suppliers.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DailyReportDTO {
    private Supplier supplier;
    private String materialCode;
    private Plant plant;
    private String deviationType;
    private Integer deviationsNr;
    private Float averageDeviation;
    private Date date;
}
