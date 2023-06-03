package com.licenta.supp_rel.reportChoices;

import com.licenta.supp_rel.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

@Entity
@Table(schema = "public", name = "report_choices")
public class ReportChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String plantId;
    private String supplierId;
    private String materialCode;
}
