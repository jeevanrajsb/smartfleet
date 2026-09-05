package com.smartfleet.ai.assignment;

import com.smartfleet.ai.common.exception.*;
import com.smartfleet.ai.delivery.Delivery;
import com.smartfleet.ai.delivery.DeliveryRepository;
import com.smartfleet.ai.delivery.DeliveryStatus;
import com.smartfleet.ai.driver.Driver;
import com.smartfleet.ai.driver.DriverRepository;
import com.smartfleet.ai.driver.DriverStatus;
import com.smartfleet.ai.vehicle.VehicleStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignmentService {

    private final DriverRepository driverRepository;
    private final DeliveryRepository deliveryRepository;
    private final AssignmentRepository assignmentRepository;

    public AssignmentService(DriverRepository driverRepository,
                            DeliveryRepository deliveryRepository,
                            AssignmentRepository assignmentRepository) {
        this.driverRepository = driverRepository;
        this.deliveryRepository = deliveryRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public Assignment assignDriver(Long deliveryId, Long driverId) {
        Driver driver = driverRepository.findByIdForUpdate(driverId)
                .orElseThrow(() -> new DriverNotFoundException(driverId));

        Delivery delivery = deliveryRepository.findByIdForUpdate(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));

        if (delivery.getAssignedDriverId() != null) {
            throw new DuplicateAssignmentException("Delivery " + deliveryId + " is already assigned to driver " + delivery.getAssignedDriverId());
        }

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new DriverUnavailableException("Driver " + driverId + " is not available for assignment.");
        }

        if (driver.getVehicle() == null || driver.getVehicle().getStatus() == VehicleStatus.OUT_OF_SERVICE) {
            throw new DriverUnavailableException("Driver " + driverId + " does not have a usable vehicle.");
        }

        if (!driver.getVehicle().getType().equals(delivery.getRequiredVehicleType())) {
            throw new VehicleCompatibilityException("Driver vehicle type does not match delivery requirement.");
        }

        if (driver.getCurrentWorkload() >= driver.getMaxConcurrentDeliveries()) {
            throw new DriverUnavailableException("Driver " + driverId + " has reached workload capacity.");
        }

        if (assignmentRepository.existsByDriverIdAndStatusIn(driverId, List.of(AssignmentStatus.ACTIVE))) {
            throw new DuplicateAssignmentException("Driver " + driverId + " already has an active assignment.");
        }

        if (assignmentRepository.existsByDeliveryIdAndStatusIn(deliveryId, List.of(AssignmentStatus.ACTIVE))) {
            throw new DuplicateAssignmentException("Delivery " + deliveryId + " already has an active assignment.");
        }

        Assignment assignment = Assignment.builder()
                .deliveryId(deliveryId)
                .driverId(driverId)
                .status(AssignmentStatus.ACTIVE)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);

        driver.setCurrentWorkload(driver.getCurrentWorkload() + 1);
        delivery.setAssignedDriverId(driverId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        driverRepository.save(driver);
        deliveryRepository.save(delivery);

        return savedAssignment;
    }
}
