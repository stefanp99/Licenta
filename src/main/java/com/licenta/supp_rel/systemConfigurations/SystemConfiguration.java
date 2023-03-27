package com.licenta.supp_rel.systemConfigurations;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_configurations", schema = "public")
public class SystemConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String configGroup;

    private String configName;

    private String configValues;

}
