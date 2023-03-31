package com.licenta.supp_rel.plants;

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
@Table(name = "plants", schema = "public")
public class Plant {
    @Id
    private String id;
    private String cityCountry;
    private String segment;
    private Float cityLongitude;
    private Float cityLatitude;

    public String getCity(){
        return cityCountry.substring(0, cityCountry.indexOf("/"));
    }

    public String getCountry(){
        return cityCountry.substring(cityCountry.indexOf("/") + 1);
    }
}
