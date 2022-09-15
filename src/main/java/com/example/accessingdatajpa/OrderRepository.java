package com.example.accessingdatajpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long>{
    Order findById(long id);
    List<Order> findAllByCustomer(Customer customer);
    List<Order> findAllByDescriptionContainsIgnoreCase(String query);

    List<Order> getOrdersByCustomer_LastName(String query);
    @Query("SELECT o.id as id, o.description as description, " +
            "o.customer.firstName as firstName, " +
            "o.customer.lastName as lastName FROM Order o")
    List<OrderInfo> findAllOrdersWithInfo();

    // JPQL query which will handle the join between
    // orders and customers
    @Query("SELECT o.id as id, o.description as description, " +
            "o.customer.firstName as firstName, " +
            "o.customer.lastName as lastName FROM Order o WHERE o.customer = :customer")
    List<OrderInfo> findAllOrdersByCustomerWithInfo(Customer customer);

}
