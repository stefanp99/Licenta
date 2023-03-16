package com.licenta.supp_rel.deliveries;

import com.licenta.supp_rel.deviations.Deviation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse {
    Delivery delivery;
    List<Deviation> deviations;
}
