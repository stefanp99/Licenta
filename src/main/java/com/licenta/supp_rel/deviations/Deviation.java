package com.licenta.supp_rel.deviations;

import com.licenta.supp_rel.deliveries.Delivery;
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
@Table(name = "deviations", schema = "public")
public class Deviation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private DeviationTypes type;
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;
    private Long quantityDiff;
    private Long timeDiff;
}
