package com.licenta.supp_rel.deliveries;

import com.licenta.supp_rel.contracts.Contract;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deliveries", schema = "public")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long expectedQuantity;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    private Timestamp dispatchDate;
    private Timestamp deliveryDate;
    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;
    private Timestamp expectedDeliveryDate;
}
