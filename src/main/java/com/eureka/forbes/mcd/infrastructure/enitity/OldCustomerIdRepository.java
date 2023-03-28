package com.eureka.forbes.mcd.infrastructure.enitity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OldCustomerIdRepository extends CrudRepository<OldCustomerId, Integer> {
    List<OldCustomerId> findAllByOldCustomerIdIn(Set<Integer> oldCustomerIds);
}
