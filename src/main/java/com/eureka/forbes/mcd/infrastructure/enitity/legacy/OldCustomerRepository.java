package com.eureka.forbes.mcd.infrastructure.enitity.legacy;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OldCustomerRepository extends CrudRepository<OldCustomer, Integer> {
}
