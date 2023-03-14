package com.licenta.supp_rel.deviations;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviationResponse {
    private Integer nrDeviations;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
