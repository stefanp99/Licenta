package com.licenta.supp_rel.suppliers;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "suppliers", schema = "public")
public class Supplier {
    @Id
    private String id;
    private String cityCountry;
    private String name;

    public String getCity(){
        return cityCountry.substring(0, cityCountry.indexOf("/"));
    }

    public String getCountry(){
        return cityCountry.substring(cityCountry.indexOf("/") + 1);
    }
}
