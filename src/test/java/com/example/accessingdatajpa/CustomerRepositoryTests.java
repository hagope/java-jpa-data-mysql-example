package com.example.accessingdatajpa;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import java.util.List;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// Uses the configured database rather than in-memory
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CustomerRepositoryTests {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Set Up Customer Record")
    @Rollback(value = false)
    @Order(1)
    // this is required to persist records in the database for future tests
    // ideally Unit tests should not have dependencies
    // however, since each test in a single transaction
    // testing Updates is non-trivial without a separate step to persist/modify records
    void setUpCustomerRecord() {
        String dataString = "{\"zip\":94089}";
        Customer c = customerRepository.save(new Customer("David", "Palmer", dataString));
        Assertions.assertThat(c.getId()).isGreaterThan(0);
        orderRepository.save(new com.example.accessingdatajpa.Order(c, "apple ipad"));
        // test mergeMetadata function
        String newDataString = "{\"zip\":94090}";
        customerRepository.mergeMetadata(c, newDataString);
        customerRepository.mergeMetadata(c, "{}");
    }

    @Test
    @DisplayName("Test Get Last Customer Record")
    @Order(2)
    void testGetLastCustomerRecord() {
        Customer customer = customerRepository.findTopByOrderByIdDesc();
        Assertions.assertThat(customer.getId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Test Find by Full Name")
    @Order(3)
    void testFindByFullName() {
        Integer records = customerRepository.findByFullName("David Palmer").toArray().length;
        Assertions.assertThat(records).isGreaterThan(0);

        Integer records2 = customerRepository.findByFullName("Palmer David").toArray().length;
        Assertions.assertThat(records2).isEqualTo(0);
    }

    @Test
    @DisplayName("Test Get All Orders")
    @Order(4)
    void testGetAllOrders() {
        List<OrderInfo> records = orderRepository.findAllOrdersWithInfo();
        Assertions.assertThat(records.toArray().length).isGreaterThan(0);
    }

    @Test
    @DisplayName("test merge new customer data")
    @Order(5)
    void testMergeNewCustomerData() {
        Customer customer = customerRepository.findTopByOrderByIdDesc();
        Assertions.assertThat(customer.getMetaData()).isEqualTo("{\"zip\": 94090}");
    }

    @Test
    @DisplayName("Clean Up")
    @Order(6)
    @Rollback(value = false)
    void cleanUp() {
        Customer customer = customerRepository.findTopByOrderByIdDesc();
        customerRepository.delete(customer);
    }
}