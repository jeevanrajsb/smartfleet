package com.smartfleet.ai.delivery;

import com.smartfleet.ai.vehicle.VehicleType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryRepository deliveryRepository;

    public DeliveryController(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @GetMapping
    public List<Delivery> getAll() {
        return deliveryRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Delivery> create(@RequestBody Delivery delivery) {
        return ResponseEntity.ok(deliveryRepository.save(delivery));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getById(@PathVariable Long id) {
        return deliveryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<Delivery> updatePriority(@PathVariable Long id, @RequestParam DeliveryPriority priority) {
        Delivery delivery = deliveryRepository.findById(id).orElseThrow();
        delivery.setPriority(priority);
        return ResponseEntity.ok(deliveryRepository.save(delivery));
    }
}
