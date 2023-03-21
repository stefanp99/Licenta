package com.licenta.supp_rel.contracts;


import com.licenta.supp_rel.plants.Plant;
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
@Table(name = "contracts", schema = "public")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    private String materialCode;
    private Float pricePerUnit;
    @ManyToOne
    @JoinColumn(name = "plant_id")
    private Plant plant;

}
