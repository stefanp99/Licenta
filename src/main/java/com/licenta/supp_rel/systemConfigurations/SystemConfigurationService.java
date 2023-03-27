package com.licenta.supp_rel.systemConfigurations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SystemConfigurationService {
    @Autowired
    SystemConfigurationRepository systemConfigurationRepository;
    public List<String> findPropertiesByGroupAndName(String configGroup, String configName, List<String> wantedValues){
        List<SystemConfiguration> systemConfigurations = systemConfigurationRepository.
                findAllByConfigGroupAndConfigName(configGroup, configName);
        List<String> returnedList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if(wantedValues.isEmpty()){
            for(SystemConfiguration systemConfiguration: systemConfigurations) {
                JsonNode rootNode;
                try {
                    rootNode = mapper.readTree(systemConfiguration.getConfigValues());
                    rootNode.fields().forEachRemaining(entry -> returnedList.add(entry.getValue().toString()));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else{
            for(SystemConfiguration systemConfiguration: systemConfigurations) {
                JsonNode rootNode;
                try {
                    rootNode = mapper.readTree(systemConfiguration.getConfigValues());
                    rootNode.fields().forEachRemaining(entry -> {
                        if(wantedValues.contains(entry.getKey()))
                            returnedList.add(entry.getValue().toString());
                    });
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return returnedList;
    }

}
