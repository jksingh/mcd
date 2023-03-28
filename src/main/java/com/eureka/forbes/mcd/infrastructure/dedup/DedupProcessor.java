package com.eureka.forbes.mcd.infrastructure.dedup;

import com.eureka.forbes.mcd.infrastructure.enitity.Customer;
import com.eureka.forbes.mcd.infrastructure.enitity.CustomerRepository;
import com.eureka.forbes.mcd.infrastructure.enitity.OldCustomerId;
import com.eureka.forbes.mcd.infrastructure.enitity.OldCustomerIdRepository;
import com.eureka.forbes.mcd.infrastructure.enitity.es.EsCustomer;
import com.eureka.forbes.mcd.infrastructure.enitity.es.EsCustomerRepository;
import com.eureka.forbes.mcd.infrastructure.enitity.legacy.OldCustomer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * For the input customer queries the ES for customer with matching phone number or address.
 * Passes on the customer to upstream if customer is not present in database.
 *
 * Makes 2 calls for old customer-ids to skip customers already added.
 * Makes 2 calls to ES to get matching customers by phone number (with score 10) or address (with score 40).
 */
@Component
public class DedupProcessor implements ItemProcessor<OldCustomer, Customer> {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OldCustomerIdRepository oldCustomerIdsRepository;
    @Autowired
    private EsCustomerRepository esCustomerRepository;

    @Override
    public Customer process(OldCustomer oldCustomer) throws Exception {
        Iterable<OldCustomerId> oldCustomerId = oldCustomerIdsRepository.findAllById(List.of(oldCustomer.getId()));
        if (oldCustomerId.iterator().hasNext()) return null;

        EsCustomer esCustomer = EsCustomer.from(oldCustomer);
        List<EsCustomer> esCustomersByPhone = esCustomerRepository.findByPhoneNumber(esCustomer.getId(),
                        Pageable.ofSize(10)).stream()
                .takeWhile(h -> h.getScore() > 10).map(SearchHit::getContent).toList();
        List<EsCustomer> esCustomersByAddress = esCustomerRepository.findByAddress(esCustomer.getId(),
                        Pageable.ofSize(10)).stream()
                .takeWhile(h -> h.getScore() > 40).map(SearchHit::getContent).toList();
        List<EsCustomer> matchedEsCustomers = Stream.concat(esCustomersByPhone.stream(), esCustomersByAddress.stream()).toList();

        List<Integer> matchedCustomerIds = matchedEsCustomers.stream().map(EsCustomer::getId).collect(Collectors.toList());
        Set<Integer> matchedOldCustomerIds = StreamSupport.stream(
                        oldCustomerIdsRepository.findAllById(matchedCustomerIds).spliterator(), false)
                .map(OldCustomerId::getOldCustomerId).collect(Collectors.toSet());

        List<EsCustomer> esCustomers = Stream.concat(Stream.of(esCustomer), matchedEsCustomers.stream())
                .filter(e -> !matchedOldCustomerIds.contains(e.getId()))
                .sorted(Comparator.comparing(e -> e.getModificationTime()))
                .collect(Collectors.toList());

        EsCustomer topEsCustomer = esCustomers.get(0);
        Customer customer = topEsCustomer.to();
        Set<OldCustomerId> oldCustomerIds = esCustomers.stream().map(e ->
                        OldCustomerId.builder().oldCustomerId(e.getId()).customer(customer).build())
                .collect(Collectors.toSet());
        customer.setOldCustomerIds(oldCustomerIds);
        return customer;
    }
}
