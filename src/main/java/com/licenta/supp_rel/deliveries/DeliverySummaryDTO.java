package com.licenta.supp_rel.deliveries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverySummaryDTO {
    private String supplierId;
    private String materialCode;
    private String plantId;
    private String deliveryDay;
    private Long totalQuantity;
    private Long nrDeliveries;
}
