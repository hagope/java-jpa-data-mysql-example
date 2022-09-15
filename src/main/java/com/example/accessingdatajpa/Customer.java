package com.example.accessingdatajpa;

import lombok.*;
import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
// Example of using  a named Query
@NamedQuery(name = "Customer.findByFullName",
        query = "SELECT c FROM Customer c WHERE CONCAT(c.firstName,' ',c.lastName) = ?1 ")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @Setter(AccessLevel.NONE)
    private Long id;
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "meta_data", columnDefinition = "json")
    private String metaData; // we don't care about mapping it just yet

    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    @ToString.Exclude
    private List<Order> orders;

    protected Customer() {}
    public Customer(String firstName, String lastName, String metaData) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.metaData =  metaData;
    }
}