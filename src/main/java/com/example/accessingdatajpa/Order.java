package com.example.accessingdatajpa;

import javax.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "description")
    private String description;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private Customer customer;

    protected Order() {}
    public Order(Customer customer, String description) {
        this.description = description;
        this.customer = customer;
    }
}
