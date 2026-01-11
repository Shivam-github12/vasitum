package com.vasitum.scheduler.controller;

import com.vasitum.scheduler.dto.BookSlotRequest;
import com.vasitum.scheduler.dto.InterviewSlotDto;
import com.vasitum.scheduler.dto.PaginatedResponse;
import com.vasitum.scheduler.service.InterviewSlotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interview-slots")
@CrossOrigin(origins = "*")
public class InterviewSlotController {

    @Autowired
    private InterviewSlotService interviewSlotService;

    @GetMapping("/available")
    public ResponseEntity<PaginatedResponse<InterviewSlotDto>> getAvailableSlots(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        
        if (limit > 100) limit = 100; // Prevent excessive load
        
        PaginatedResponse<InterviewSlotDto> response = interviewSlotService.getAvailableSlots(cursor, limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/book")
    public ResponseEntity<InterviewSlotDto> bookSlot(@Valid @RequestBody BookSlotRequest request) {
        InterviewSlotDto slot = interviewSlotService.bookSlot(request);
        return ResponseEntity.ok(slot);
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<InterviewSlotDto> updateSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody BookSlotRequest request) {
        InterviewSlotDto slot = interviewSlotService.updateSlot(slotId, request);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping("/{slotId}/cancel")
    public ResponseEntity<Void> cancelSlot(@PathVariable Long slotId) {
        interviewSlotService.cancelSlot(slotId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<InterviewSlotDto> getSlot(@PathVariable Long slotId) {
        InterviewSlotDto slot = interviewSlotService.getSlot(slotId);
        return ResponseEntity.ok(slot);
    }

    @GetMapping("/interviewer/{interviewerId}")
    public ResponseEntity<List<InterviewSlotDto>> getSlotsByInterviewer(@PathVariable Long interviewerId) {
        List<InterviewSlotDto> slots = interviewSlotService.getSlotsByInterviewer(interviewerId);
        return ResponseEntity.ok(slots);
    }
}