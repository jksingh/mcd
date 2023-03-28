package com.eureka.forbes.mcd.infrastructure.enitity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer> {
    List<Customer> findAllByPhoneNumberIn(Set<String> phoneNumbers);
}
