package com.example.accessingdatajpa;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerRepository extends CrudRepository<Customer, Long>{
    // magic "Query Methods" provided by spring data jpa
    // these do NOT need to be implemented
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
    List<Customer> findByLastName(String lastName);
    Customer findById(long id);
    Customer findTopByOrderByIdDesc();

    // Using a named query in Customer.java
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.named-queries
    List<Customer> findByFullName(String fullName);

    @Modifying
    @Transactional
    // Example of a nativeQuery, use with caution, is not re-factor safe.
//    @Query(value = "UPDATE customer SET meta_data = JSON_MERGE_PATCH(meta_data, ?2) WHERE id=?1", nativeQuery = true)
    // Much better implementation using JPQL with FUNCTION(). JSON_MERGE_PATCH is MySQL specific.
    @Query("UPDATE Customer c SET c.metaData = FUNCTION('JSON_MERGE_PATCH', c.metaData, :data) WHERE c = :customer")
    void mergeMetadata(Customer customer, String data);
}