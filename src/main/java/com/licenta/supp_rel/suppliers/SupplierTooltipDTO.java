package com.licenta.supp_rel.suppliers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierTooltipDTO {
    private String id;
    private String name;
    private String cityCountry;
    private Integer totalNumberDeliveries;
    private Float correctDeliveriesPercentage;
    private Float qtyDeviationCurveRating;
    private Float dayDeviationCurveRating;
    private Float averageNumberOfHoursToDeliver;
}
