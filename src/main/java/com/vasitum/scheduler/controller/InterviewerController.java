package com.vasitum.scheduler.controller;

import com.vasitum.scheduler.dto.CreateInterviewerRequest;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.service.InterviewerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviewers")
@CrossOrigin(origins = "*")
public class InterviewerController {

    @Autowired
    private InterviewerService interviewerService;

    @PostMapping
    public ResponseEntity<Interviewer> createInterviewer(@Valid @RequestBody CreateInterviewerRequest request) {
        Interviewer interviewer = interviewerService.createInterviewer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interviewer> getInterviewer(@PathVariable Long id) {
        Interviewer interviewer = interviewerService.getInterviewer(id);
        return ResponseEntity.ok(interviewer);
    }

    @GetMapping
    public ResponseEntity<List<Interviewer>> getAllInterviewers() {
        List<Interviewer> interviewers = interviewerService.getAllInterviewers();
        return ResponseEntity.ok(interviewers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Interviewer> updateInterviewer(
            @PathVariable Long id,
            @Valid @RequestBody CreateInterviewerRequest request) {
        Interviewer interviewer = interviewerService.updateInterviewer(id, request);
        return ResponseEntity.ok(interviewer);
    }

    @PostMapping("/{id}/generate-slots")
    public ResponseEntity<Void> generateSlots(@PathVariable Long id) {
        interviewerService.generateInterviewSlots(id);
        return ResponseEntity.ok().build();
    }
}