package com.licenta.supp_rel.contracts;

import com.licenta.supp_rel.suppliers.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Integer> {
    List<Contract> findAllBySupplier(Supplier supplier);

    List<Contract> findAllBySupplierAndMaterialCode(Supplier supplier, String materialCode);

    List<Contract> findAllByMaterialCode(String materialCode);
}
