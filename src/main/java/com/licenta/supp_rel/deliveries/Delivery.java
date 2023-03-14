package com.licenta.supp_rel.deliveries;

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

    private String materialCode;
    private String plantId;
    private String supplierId;
    private Float pricePerUnit;
    private Long quantity;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    private Timestamp dispatchDate;
    private Timestamp deliveryDate;
}
