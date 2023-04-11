package com.licenta.supp_rel.systemConfigurations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("system-configurations")
@RequiredArgsConstructor
public class SystemConfigurationsController {
    @Autowired
    SystemConfigurationRepository systemConfigurationRepository;

    @GetMapping("get-by-group-and-name")
    public List<SystemConfiguration> getByConfigGroupAndName(@RequestParam(value = "configGroup", required = false) String configGroup,
                                                             @RequestParam(value = "configName", required = false) String configName){
        List<SystemConfiguration> returnedList;
        if(configName == null || configName.equals("")) {
            if (configGroup == null || configGroup.equals(""))
                returnedList = systemConfigurationRepository.findAll();
            else
                returnedList = systemConfigurationRepository.findAllByConfigGroup(configGroup);
            returnedList.sort(Comparator.comparing(SystemConfiguration::getConfigGroup).thenComparing(SystemConfiguration::getConfigName));
            return returnedList;
        }
        returnedList = systemConfigurationRepository.findAllByConfigGroupAndConfigName(configGroup, configName);
        returnedList.sort(Comparator.comparing(SystemConfiguration::getConfigGroup).thenComparing(SystemConfiguration::getConfigName));
        return returnedList;

    }
    @PutMapping("modify")
    public SystemConfiguration modifyConfiguration(@RequestParam("configGroup") String configGroup,
                                                   @RequestParam("configName") String configName,
                                                   @RequestParam("newConfig") String newConfig){
        List<SystemConfiguration> systemConfigurations =
                systemConfigurationRepository.findAllByConfigGroupAndConfigName(configGroup, configName);
        SystemConfiguration systemConfiguration = null;
        if(systemConfigurations.size() > 0)
            systemConfiguration = systemConfigurations.get(0);
        if(systemConfiguration != null){
            systemConfiguration.setConfigValues(newConfig);
            systemConfigurationRepository.save(systemConfiguration);
            return systemConfiguration;
        }
        return null;
    }
}
