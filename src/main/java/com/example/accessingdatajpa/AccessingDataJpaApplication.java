package com.example.accessingdatajpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class AccessingDataJpaApplication {

    private static final Logger log = LoggerFactory.getLogger(AccessingDataJpaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AccessingDataJpaApplication.class);
    }

    @Bean
    public CommandLineRunner demo(CustomerRepository customerRepo, OrderRepository orderRepo) {
        return (args) -> {
            // save a few customers
            Customer jack = new Customer("Jack", "Bauer", "{\"zip\":94087}");
            Customer kim = new Customer("Kim", "Bauer", "{}");
            Customer chloe = new Customer("Chloe", "O'Brian", "{}");
            customerRepo.save(jack);
            customerRepo.save(chloe);
            customerRepo.save(kim);
            customerRepo.save(new Customer("David", "Palmer", "{}"));
            customerRepo.save(new Customer("Michelle", "Dean", "{}"));

            // make some orders
            orderRepo.save(new Order(jack, "shoes"));
            orderRepo.save(new Order(jack, "shirt"));
            orderRepo.save(new Order(jack, "apple ipad"));
            orderRepo.save(new Order(kim, "apple iphone"));
            orderRepo.save(new Order(kim, "book"));
            orderRepo.save(new Order(kim, "tv"));
            orderRepo.save(new Order(chloe, "apple mac"));

            // fetch first customer
            log.info("Customers found with findById(1L):");
            log.info("-------------------------------");
            Customer customer1 = customerRepo.findById(1L);
            log.info(customer1.toString());
            log.info("");

            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            for(Customer customer : customerRepo.findAll()) {
                log.info(customer.toString());
            }
            log.info("");

            // fetch last customer in table
            log.info("Last Customer with findTopByOrderByIdDesc():");
            log.info("-------------------------------");
            Customer lastCustomer = customerRepo.findTopByOrderByIdDesc();
            Long lastCustomerId = lastCustomer.getId();
            log.info(String.format("Customer found with findById(%d):", lastCustomerId));
            Optional<Customer> customer = customerRepo.findById(lastCustomerId);
            log.info("--------------------------------");
            log.info(customer.toString());
            log.info("");

            // fetch customers by last name
            log.info("Customers found with findByLastName('Bauer'):");
            log.info("--------------------------------------------");
            customerRepo.findByLastName("Bauer").forEach(bauer -> log.info(bauer.toString()));
            log.info("");

            // get all of Jack's orders
            log.info("All of Jack's Orders with findAllByCustomer(jack)");
            log.info("--------------------------------------------");
            orderRepo.findAllByCustomer(jack).forEach(order -> log.info(order.toString()));

            // get all apple orders
            log.info("All Apple Orders with findAllByDescriptionContainsIgnoreCase('apple')");
            log.info("--------------------------------------------");
            orderRepo.findAllByDescriptionContainsIgnoreCase("apple").forEach(order -> log.info(order.toString()));

            // get all Bauer orders
            log.info("All Bauer Orders with getOrdersByCustomer_LastName('Bauer')");
            log.info("--------------------------------------------");
            orderRepo.getOrdersByCustomer_LastName("Bauer").forEach(order -> log.info(order.toString()));

            // get all orders with more info
            log.info("All Orders with more data with findAllWithInfo()");
            log.info("--------------------------------------------");
            orderRepo.findAllOrdersWithInfo().forEach(orderInfo -> log.info(orderInfo.getId() + ":" + orderInfo.getDescription()
                    + ":" + orderInfo.getFirstName()
                    + " " + orderInfo.getLastName()));

            // get all orders of jack with more info
            log.info("All Jack's Orders with more data with findAllByCustomerWithInfo(jack)");
            log.info("--------------------------------------------");
            orderRepo.findAllOrdersByCustomerWithInfo(jack).forEach(orderInfo -> log.info(orderInfo.getId() + ":" + orderInfo.getDescription()
                    + ":" + orderInfo.getFirstName()
                    + " " + orderInfo.getLastName()));

            // get all customer by full name using named query
            log.info("All Jack's Orders with more data with findByFullName('Jack Bauer')");
            log.info("--------------------------------------------");
            customerRepo.findByFullName("Jack Bauer").forEach(c -> log.info(c.toString()));
            // System.in.read();

            // patch metadata
            log.info("Update Jack's meta_data");
            log.info("--------------------------------------------");
            log.info(jack.toString());
            customerRepo.mergeMetadata(jack, "{\"zip\": 94086}"); // existing keys updated
            customerRepo.mergeMetadata(jack, "{\"country\": \"CA\"}"); //  new keys added
            customerRepo.mergeMetadata(jack, "{}"); // empty json has no effect
            // fetch from db
            Optional<Customer> jackUpdated = customerRepo.findById(jack.getId());
            log.info(jackUpdated.toString());
        };
    }
}