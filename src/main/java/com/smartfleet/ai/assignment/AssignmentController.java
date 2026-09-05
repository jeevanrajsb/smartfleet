package com.smartfleet.ai.assignment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentRepository assignmentRepository, AssignmentService assignmentService) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<Assignment> getAll() {
        return assignmentRepository.findAll();
    }

    @PostMapping("/assign")
    public ResponseEntity<Assignment> assign(@RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.assignDriver(request.deliveryId(), request.driverId()));
    }
}
