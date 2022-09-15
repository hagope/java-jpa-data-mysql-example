# Getting Started
The purpose of this repo is to demonstrate how to persist data in a relational MySQL database using the Spring Data JPA APIs. This sample application covers some common patterns when dealing with relational data including:

- Relationships between entities
- Querying across entities (ie joins)
- Handling JSON data type in MySQL and merging
- Unit Testing (WIP)

To get started with this app, spin up the database locally via docker using:
```
docker-compose up -d
```
Then to run the Java app:
```
./gradlew bootRun
```
and to run the unit-tests:
```
./gradlew test
```
### Relationships between entities
Spring Data JPA can easily handle relationships between different entities if the entities are properly annotated. For example, consider the relationships between `Customer` and `Orders`. We would like to represent the fact that a `Customer` can have many `Orders`. 

Below in the `Customer.java` file, you can see that `Customer` is annotated as `@OneToMany` and similarly `Order.java` is annotated as `@ManyToOne`
```java
// Customer.java
@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
@JoinColumn(name = "customer_id", referencedColumnName = "id")
@ToString.Exclude
private List<Order> orders;
```

```java
// Order.java
@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
@JoinColumn(name = "customer_id")
@ToString.Exclude
private Customer customer;
```

### Querying across entities (ie joins)
After setting up the entity relationships, querying across entities (ie joining tables in the database) is now easy. For example, to implement a method `findAllOrdersByCustomerWithInfo(Customer customer)` which fetches a list of orders for a customer, you can use a JPQL query in `OrderRepository.java`

```jpaql
SELECT o.id as id, o.description as description, o.customer.firstName as firstName, o.customer.lastName as lastName FROM Order o WHERE o.customer = :customer
```
Hibernate generated SQL query:

```sql
select order0_.id as col_0_0_, order0_.description as col_1_0_, customer1_.first_name as col_2_0_, customer1_.last_name as col_3_0_ from orders order0_ cross join customer customer1_ where order0_.customer_id=customer1_.id and order0_.customer_id=?
```

### Handling JSON data type in MySQL and merging new data
Our application has a `meta_data` field that stores additional information about the Customer in JSON format in `Customer.java`. We don't have a specific schema in mind, for demonstration purposes we'll use a `String` type, although in a common application you would use `HashMap` in order to parse and manipulate the JSON data structure.

```java
@Column(name = "meta_data", columnDefinition = "json")
private String metaData; // we don't care about mapping it just yet
```

Since we don't care about the schema, we want to be able to update and add any additional JSON elements in the `meta_data` payload. This can be accomplished using the MySQL-specific function [JSON_MERGE_PATCH](https://dev.mysql.com/doc/refman/5.7/en/json-modification-functions.html#function_json-merge-patch). _NOTE: this is a database-specific function that is being hard-coded into ourJava application, so any backend migrations will need to update this code._

Since this is a database function that we'll need to call, our query will need to be hand coded, (we can't rely on any magical [JPA Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation) to accomplish this task. We'll need to rely on a Query to implement our method `mergeMetadata(Customer customer, String data)` to update the `meta_data` JSON in the databases with new/changed data. 

With a *Native Query*, the least preferred option, the SQL query is hard-coded in `CustomerRepository.java`:
```sql
UPDATE customer SET meta_data = JSON_MERGE_PATCH(meta_data, ?2) WHERE id=?1
```
Notice that the tables names are hard-coded here rather than referencing the Java objects. _Avoid Native queries at all costs as they are NOT refactor-safe and any changes to your back-end schema may need to be updated here as well (ie table/column renames)._

With a (JPQL) *Query*, the JPA entities are referenced by the model in `CustomerRepository.java`. However, since JPQL doesn't support more advanced JSON functions, we'll need to pass the MySQL-specific function via the `FUNCTION()` method:
```jpaql
UPDATE Customer c SET c.metaData = FUNCTION('JSON_MERGE_PATCH', c.metaData, :data) WHERE c = :customer
```
This code *is* refactor-safe, however, any database migrations, say from MySQL to PostgresQL, may require the function parameters to be updated as part of the migration.

### Unit Testing
Unit testing this application is a bit tricky due to our back-end specific function calls. We cannot rely on an H2 in-memory database, but we can get around this with the following annotation in the test class definition.
```
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
```

When developing locally, you will need to have a MySQL instance available (docker container for example) to run tests.

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.3/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.3/gradle-plugin/reference/html/#build-image)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.7.3/reference/htmlsingle/#using.devtools)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.7.3/reference/htmlsingle/#appendix.configuration-metadata.annotation-processor)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.7.3/reference/htmlsingle/#data.sql.jpa-and-spring-data)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

