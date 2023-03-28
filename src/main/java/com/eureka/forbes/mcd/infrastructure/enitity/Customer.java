package com.eureka.forbes.mcd.infrastructure.enitity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private Date dob;
    private String phoneNumber;
    private String address;
    private Timestamp creationTime;
    private Timestamp modificationTime;

    @OneToMany(mappedBy = "customer")
    private Set<OldCustomerId> oldCustomerIds;
}
