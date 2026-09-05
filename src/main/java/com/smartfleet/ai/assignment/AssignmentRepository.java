package com.smartfleet.ai.assignment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    boolean existsByDeliveryIdAndStatusIn(Long deliveryId, List<AssignmentStatus> statuses);

    boolean existsByDriverIdAndStatusIn(Long driverId, List<AssignmentStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Assignment a where a.deliveryId = :deliveryId and a.status = :status")
    Optional<Assignment> findActiveByDeliveryIdForUpdate(@Param("deliveryId") Long deliveryId, @Param("status") AssignmentStatus status);
}
