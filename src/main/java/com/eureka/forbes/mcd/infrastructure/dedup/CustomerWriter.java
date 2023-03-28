package com.eureka.forbes.mcd.infrastructure.dedup;

import com.eureka.forbes.mcd.infrastructure.enitity.Customer;
import com.eureka.forbes.mcd.infrastructure.enitity.CustomerRepository;
import com.eureka.forbes.mcd.infrastructure.enitity.OldCustomerId;
import com.eureka.forbes.mcd.infrastructure.enitity.OldCustomerIdRepository;
import jakarta.transaction.Transactional;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Save chunk of customers into database.
 * Skips customer or old customer already mapped in database.
 *
 * Make 1 call each to customers and oldcustomerid to check for customers already added by other thread.
 * Make 1 call each to save customers and oldcustomer in batch.
 */
@Component
public class CustomerWriter {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OldCustomerIdRepository oldCustomerIdsRepository;

    @Transactional
    public void saveToDb(Chunk<? extends Customer> chunk) {
        Set<String> phoneNumbers = chunk.getItems().stream().map(c -> c.getPhoneNumber()).collect(Collectors.toSet());
        List<Customer> addedCustomers = customerRepository.findAllByPhoneNumberIn(phoneNumbers);

        Map<String, List<Customer>> addedCustomerPhoneNos = addedCustomers.stream()
                .collect(Collectors.groupingBy(c -> c.getPhoneNumber()));
        Map<String, List<Customer>> newCustomerByPhoneNo = chunk.getItems().stream()
                .filter(c -> !addedCustomerPhoneNos.containsKey(c.getPhoneNumber()))
                .collect(Collectors.groupingBy(c -> c.getPhoneNumber()));
        List<Customer> newCustomers = newCustomerByPhoneNo.entrySet().stream().map(e -> {
            Customer first = e.getValue().get(0);
            Stream<OldCustomerId> oldCustomerIdStream = e.getValue().stream().skip(1).flatMap(c -> {
                c.getOldCustomerIds().forEach(i -> i.setCustomer(first));
                return c.getOldCustomerIds().stream();
            });
            Set<OldCustomerId> oldCustomerIdSet = Stream.concat(first.getOldCustomerIds().stream(),
                    oldCustomerIdStream).collect(Collectors.toSet());
            first.setOldCustomerIds(oldCustomerIdSet);
            return first;
        }).collect(Collectors.toList());

        customerRepository.saveAll(newCustomers);

        Stream<OldCustomerId> addedCustomerIdStream = chunk.getItems().stream()
                .filter(c -> addedCustomerPhoneNos.containsKey(c.getPhoneNumber()))
                .flatMap(e -> {
                    var ac = addedCustomerPhoneNos.get(e.getPhoneNumber()).get(0);
                    e.getOldCustomerIds().forEach(o -> o.setCustomer(ac));
                    return e.getOldCustomerIds().stream();
                });
        Stream<OldCustomerId> newCustomerIdStream = newCustomers.stream()
                .flatMap(e -> e.getOldCustomerIds().stream());
        List<OldCustomerId> oldCustomers = Stream.concat(addedCustomerIdStream, newCustomerIdStream)
                .collect(Collectors.toList());

        Set<Integer> oldCustomerIds = oldCustomers.stream().map(c -> c.getOldCustomerId()).collect(Collectors.toSet());
        Set<Integer> addedOldCustomers = oldCustomerIdsRepository.findAllByOldCustomerIdIn(oldCustomerIds).stream()
                .map(c -> c.getOldCustomerId()).collect(Collectors.toSet());
        Map<Object, List<OldCustomerId>> oldCustomerIdById = oldCustomers.stream()
                .filter(c -> !addedOldCustomers.contains(c.getOldCustomerId()))
                .collect(Collectors.groupingBy(c -> c.getOldCustomerId()));
        List<OldCustomerId> newOldCustomers = oldCustomerIdById.entrySet().stream().map(e -> e.getValue().get(0)).toList();

        oldCustomerIdsRepository.saveAll(newOldCustomers);
    }
}
