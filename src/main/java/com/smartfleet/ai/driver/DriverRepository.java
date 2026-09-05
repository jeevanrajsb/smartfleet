package com.smartfleet.ai.driver;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Driver d join fetch d.vehicle where d.id = :id")
    Optional<Driver> findByIdForUpdate(@Param("id") Long id);

    @Query("select d from Driver d join fetch d.vehicle where d.status = :status and d.currentWorkload < d.maxConcurrentDeliveries")
    List<Driver> findAvailableDrivers(@Param("status") DriverStatus status);
}
