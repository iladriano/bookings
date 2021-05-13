# README #

## Reservation service ##

### Assumptions ###
 * All dates are in the `Atlantic/Bermuda` Timezone
 * All dates are at `12 noon`
 * Bookings have a min of 1 and max of 3 consecutive days
 * Reservations can be created/updated from 1 day up to 1 month in advance
 * Global capacity is 1 slot daily
 * No authentication required
 * Check-in and check-out only store date parts

### Components ###
 * JDK 16+
 * Maven wrapper
 * Spring Boot Application
 * Data orm/jpa/hibernate/h2
 * Concurrency via MVCC with `@Transactional` declaration
 * Lombok for builders, accessors, `toString` and other boilerplate

### Layout ###
 * Main package contains the Spring Boot Application
 * Package `orm` contains entity models and jpa repositories
 * Package `rest` contains request/response models and rest controller
 * Package `service` contains business logic
 * Tests use H2 in-memory instead of pure unit tests

### Data model ###
Bookings
```
booking id => check-in, check-out, name/email, status
```
Dates
```
date id => booking id, status
```


### How do I get set up? ###
To get started using maven in the command-line:
```
./mvnw clean test
```
