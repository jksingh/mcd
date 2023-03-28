package com.eureka.forbes.mcd.infrastructure.enitity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OldCustomerId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int oldCustomerId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;
}
