package com.licenta.supp_rel.charts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupedVerticalBarChartDTO {
    private String name;
    private List<VerticalBarChartDTO> series;
}
