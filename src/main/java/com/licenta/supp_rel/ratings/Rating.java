package com.licenta.supp_rel.ratings;

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
@Table(name = "ratings", schema = "public")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String materialCode;
    private Float qtyPercentageRating;
    private Float qtyNrDevisRating;
    private Float dayPercentageRating;
    private Float dayNrDevisRating;
}
