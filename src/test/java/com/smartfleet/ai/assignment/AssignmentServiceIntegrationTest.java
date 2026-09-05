package com.smartfleet.ai.assignment;

import com.smartfleet.ai.delivery.Delivery;
import com.smartfleet.ai.delivery.DeliveryPriority;
import com.smartfleet.ai.delivery.DeliveryRepository;
import com.smartfleet.ai.delivery.DeliveryStatus;
import com.smartfleet.ai.driver.Driver;
import com.smartfleet.ai.driver.DriverRepository;
import com.smartfleet.ai.driver.DriverStatus;
import com.smartfleet.ai.vehicle.Vehicle;
import com.smartfleet.ai.vehicle.VehicleRepository;
import com.smartfleet.ai.vehicle.VehicleStatus;
import com.smartfleet.ai.vehicle.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AssignmentServiceIntegrationTest {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentService assignmentService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        deliveryRepository.deleteAll();
        driverRepository.deleteAll();
        vehicleRepository.deleteAll();

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .type(VehicleType.VAN)
                .capacity(5)
                .status(VehicleStatus.AVAILABLE)
                .build());

        driver = driverRepository.save(Driver.builder()
                .name("Alice")
                .status(DriverStatus.AVAILABLE)
                .vehicle(vehicle)
                .currentWorkload(0)
                .maxConcurrentDeliveries(1)
                .build());
    }

    @Test
    void shouldPreventDuplicateAssignmentsForSameDriverInConcurrentRequests() throws Exception {
        Delivery deliveryOne = deliveryRepository.save(Delivery.builder()
                .pickupLatitude(12.9)
                .pickupLongitude(77.6)
                .dropLatitude(12.95)
                .dropLongitude(77.62)
                .priority(DeliveryPriority.HIGH)
                .requiredVehicleType(VehicleType.VAN)
                .status(DeliveryStatus.PENDING)
                .build());

        Delivery deliveryTwo = deliveryRepository.save(Delivery.builder()
                .pickupLatitude(12.91)
                .pickupLongitude(77.63)
                .dropLatitude(12.97)
                .dropLongitude(77.68)
                .priority(DeliveryPriority.NORMAL)
                .requiredVehicleType(VehicleType.VAN)
                .status(DeliveryStatus.PENDING)
                .build());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Callable<Boolean>> tasks = List.of(
                () -> assignAndCapture(deliveryOne.getId()),
                () -> assignAndCapture(deliveryTwo.getId())
        );

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        executor.shutdown();

        int successCount = 0;
        for (Future<Boolean> future : results) {
            boolean succeeded = future.get();
            if (succeeded) {
                successCount++;
            }
        }

        assertThat(successCount).isEqualTo(1);
        assertThat(assignmentRepository.count()).isEqualTo(1);
    }

    private boolean assignAndCapture(Long deliveryId) {
        try {
            assignmentService.assignDriver(deliveryId, driver.getId());
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
