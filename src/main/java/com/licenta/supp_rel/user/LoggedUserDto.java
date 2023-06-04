package com.licenta.supp_rel.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoggedUserDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String username;
    private String emailAddress;
}
