package com.licenta.supp_rel.reportChoices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class ReportChoiceWithoutUserDTO {
    private Integer id;
    private String plantId;
    private String supplierId;
    private String materialCode;
}
