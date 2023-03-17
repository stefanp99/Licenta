package com.licenta.supp_rel.tolerances;

import com.licenta.supp_rel.suppliers.Supplier;
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

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    private String materialCode;
    private Float qtyUpperLimit;
    private Float qtyLowerLimit;
    private Integer dayUpperLimit;
    private Integer dayLowerLimit;
    private String plantId;
}
