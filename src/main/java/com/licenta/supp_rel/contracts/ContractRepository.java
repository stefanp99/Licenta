package com.licenta.supp_rel.contracts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Integer> {
    List<Contract> findAllByMaterialCodeAndPlantId(String materialCode, String plantId);
}
