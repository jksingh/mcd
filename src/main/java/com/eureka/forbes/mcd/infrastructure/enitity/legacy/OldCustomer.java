package com.eureka.forbes.mcd.infrastructure.enitity.legacy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "old_customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OldCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private Date dob;
    private String phoneNumber;
    private String address;
    private Timestamp creationTime;
    private Timestamp modificationTime;
}
