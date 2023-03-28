package com.eureka.forbes.mcd.infrastructure.enitity.es;

import com.eureka.forbes.mcd.infrastructure.enitity.Customer;
import com.eureka.forbes.mcd.infrastructure.enitity.legacy.OldCustomer;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.sql.Timestamp;
import java.util.Date;

@Document(indexName = "es_customer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsCustomer {
    @Id
    private int id;
    private String name;
    private long dob;
    private String phoneNumber;
    private String address;
    private long creationTime;
    private long modificationTime;

    public static EsCustomer from(OldCustomer o) {
        String phoneNumber = o.getPhoneNumber().replace("-", "");
        return EsCustomer.builder()
                .id(o.getId())
                .name(o.getName())
                .dob(o.getDob().getTime())
                .phoneNumber(phoneNumber)
                .address(o.getAddress())
                .creationTime(o.getCreationTime().getTime())
                .modificationTime(o.getModificationTime().getTime())
                .build();
    }

    public Customer to() {
        return Customer.builder()
                .name(getName())
                .dob(new Date(getDob()))
                .phoneNumber(getPhoneNumber())
                .address(getAddress())
                .creationTime(new Timestamp(getCreationTime()))
                .modificationTime(new Timestamp(getModificationTime()))
                .build();
    }
}
