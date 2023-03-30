package com.licenta.supp_rel.systemConfigurations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//{"minDeliveries": 3,"medPercQtyMinusWeight": 0.80,"medPercQtyPlusWeight": 0.20,"qtyMinusDeviNumberWeight": 0.90,
// "qtyPlusDeviNumberWeight": 0.10,"medDaysDayMinusWeight": 0.70,"medDaysDayPlusWeight": 0.30,
// "dayMinusDeviNumberWeight": 0.75,"dayPlusDeviNumberWeight": 0.25,"qtyDeviWeight": 0.50,"dayDeviWeight": 0.50,"priceWeight": 0}
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingsWeightageDTO {
    private Float minDeliveries;
    private Float medPercQtyMinusWeight;
    private Float medPercQtyPlusWeight;
    private Float qtyMinusDeviNumberWeight;
    private Float qtyPlusDeviNumberWeight;
    private Float medDaysDayMinusWeight;
    private Float medDaysDayPlusWeight;
    private Float dayMinusDeviNumberWeight;
    private Float dayPlusDeviNumberWeight;
    private Float qtyDeviWeight;
    private Float dayDeviWeight;
    private Float priceWeight;
}
