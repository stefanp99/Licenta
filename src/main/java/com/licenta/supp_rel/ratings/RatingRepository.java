package com.licenta.supp_rel.ratings;

import com.licenta.supp_rel.suppliers.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Integer> {
    @Query("SELECT COUNT(r) > 0 FROM Rating r WHERE r.supplier.id = :supplierId " +
            "AND r.materialCode = :materialCode")
    boolean existsBySupplierAndMaterialCodeAndQtyPercentageRatingAndQtyNrDevisRatingAndDayPercentageRatingAndDayNrDevisRating(
            @Param("supplierId") String supplierId,
            @Param("materialCode") String materialCode
    );

    Optional<Rating> findBySupplierAndMaterialCode(Supplier supplier, String materialCode);

}

