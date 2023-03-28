package com.eureka.forbes.mcd.infrastructure.enitity.es;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EsCustomerRepository extends ElasticsearchRepository<EsCustomer, Integer> {

    @Query("{\"more_like_this\" : { \"fields\" : [\"address\"], \"like\": [{\"_index\": \"es_customer\"," +
            "\"_id\": \"?0\"}], \"min_term_freq\" : 1,\"max_query_terms\" : 12}}, \"min_score\": 40")
    List<SearchHit<EsCustomer>> findByAddress(int id, Pageable pageable);

    @Query("{\"more_like_this\" : { \"fields\" : [\"phoneNumber\"], \"like\": [{\"_index\": \"es_customer\"," +
            "\"_id\": \"?0\"}], \"min_term_freq\" : 1,\"max_query_terms\" : 12}}")
    List<SearchHit<EsCustomer>> findByPhoneNumber(int id, Pageable pageable);
}
