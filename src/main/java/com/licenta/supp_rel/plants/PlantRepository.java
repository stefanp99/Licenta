package com.licenta.supp_rel.plants;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, String> {
    List<Plant> findBySegment(String segment);
}
