package com.licenta.supp_rel.charts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerticalBarChartDTO {
    private String name;
    private Float value;
}
