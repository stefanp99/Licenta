package com.licenta.supp_rel.tolerances;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tolerances", schema = "public")
public class Tolerance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String supplierId;
    private String materialCode;
    private Integer qtyUpperLimit;
    private Integer qtyLowerLimit;
    private Integer dayUpperLimit;
    private Integer dayLowerLimit;
    private String plantId;
}
