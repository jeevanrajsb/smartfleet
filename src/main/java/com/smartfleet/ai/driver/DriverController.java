package com.smartfleet.ai.driver;

import com.smartfleet.ai.vehicle.Vehicle;
import com.smartfleet.ai.vehicle.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
public class DriverController {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverController(DriverRepository driverRepository, VehicleRepository vehicleRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping
    public List<Driver> getAll() {
        return driverRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Driver> create(@RequestBody Driver driver) {
        if (driver.getVehicle() != null && driver.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(driver.getVehicle().getId()).orElseThrow();
            driver.setVehicle(vehicle);
        }
        return ResponseEntity.ok(driverRepository.save(driver));
    }
}
