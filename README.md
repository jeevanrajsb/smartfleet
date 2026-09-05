# SmartFleet AI

A modular monolith for a real-time fleet and delivery orchestration platform.

## Stage 1: core fleet domain and concurrency-safe assignment guard

This first stage focuses on the following architectural decisions:

- Keep a modular monolith structure so domain boundaries are explicit and later extractable.
- Use JPA entities and repositories for the driver, vehicle, delivery, and assignment domains.
- Apply pessimistic locking at the assignment boundary (`findByIdForUpdate`) to serialize concurrent assignment decisions for the same driver or delivery.
- Use optimistic locking (`@Version`) on the persistence model so optimistic conflict detection remains available during later business-rule changes.
- Keep Redis integration ready for later distributed coordination, but do not force Redis locks into every transaction. This stage demonstrates the correct layer for locking: the database row-level lock at the assignment decision point.

## Running the application

```bash
mvn test
mvn spring-boot:run
```

## Main API

- `POST /api/v1/drivers`
- `GET /api/v1/drivers`
- `POST /api/v1/vehicles`
- `GET /api/v1/vehicles`
- `POST /api/v1/deliveries`
- `GET /api/v1/deliveries`
- `POST /api/v1/assignments/assign`
