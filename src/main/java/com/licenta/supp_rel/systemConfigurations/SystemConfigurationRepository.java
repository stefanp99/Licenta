package com.licenta.supp_rel.systemConfigurations;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Integer> {
    List<SystemConfiguration> findAllByConfigGroupAndConfigName(String configGroup, String configName);
}
