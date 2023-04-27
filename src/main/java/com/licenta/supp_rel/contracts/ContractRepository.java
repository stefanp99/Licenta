package com.licenta.supp_rel.contracts;

import com.licenta.supp_rel.plants.Plant;
import com.licenta.supp_rel.suppliers.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Integer> {
    List<Contract> findAllBySupplier(Supplier supplier);

    List<Contract> findAllBySupplierAndMaterialCode(Supplier supplier, String materialCode);
    List<Contract> findAllBySupplierAndMaterialCodeAndPlant(Supplier supplier, String materialCode, Plant plant);
    List<Contract> findAllBySupplierAndPlant(Supplier supplier, Plant plant);
    List<Contract> findAllByMaterialCode(String materialCode);
    List<Contract> findAllByMaterialCodeAndPlant(String materialCode, Plant plant);

    @Query(value = "select distinct c.materialCode from Contract c order by c.materialCode asc")
    List<String> findAllMaterialCodes();

}
