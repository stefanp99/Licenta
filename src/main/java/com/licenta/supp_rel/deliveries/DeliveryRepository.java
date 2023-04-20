package com.licenta.supp_rel.deliveries;

import com.licenta.supp_rel.contracts.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByContract(Contract contract);

    List<Delivery> findByContractAndStatus(Contract contract, DeliveryStatus status);

    @Query("SELECT NEW com.licenta.supp_rel.deliveries.DeliverySummaryDTO(" +
            "c.supplier.id, c.materialCode, c.plant.id, " +
            "function('to_char', d.deliveryDate, 'yyyy-MM-dd'), " +
            "SUM(d.realQuantity), COUNT(d.id)) " +
            "FROM Delivery d " +
            "JOIN d.contract c " +
            "WHERE d.status = 'delivered' " +
            "AND c.supplier.id IN (:supplierIds) " +
            "AND c.materialCode IN (:materialCodes) " +
            "AND c.plant.id IN (:plantIds) " +
            "GROUP BY c.supplier.id, c.materialCode, c.plant.id, " +
            "function('to_char', d.deliveryDate, 'yyyy-MM-dd')")
    List<DeliverySummaryDTO> findDeliveriesBySupplierMaterialPlant(
            @Param("supplierIds") List<String> supplierIds,
            @Param("materialCodes") List<String> materialCodes,
            @Param("plantIds") List<String> plantIds);


}
