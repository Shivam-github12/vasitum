package com.vasitum.scheduler.service;

import com.vasitum.scheduler.dto.AvailabilitySlotDto;
import com.vasitum.scheduler.dto.CreateInterviewerRequest;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.exception.ResourceNotFoundException;
import com.vasitum.scheduler.repository.InterviewerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InterviewerServiceTest {

    @Autowired
    private InterviewerService interviewerService;

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Test
    void testCreateInterviewer() {
        List<AvailabilitySlotDto> slots = Arrays.asList(
            new AvailabilitySlotDto(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)),
            new AvailabilitySlotDto(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
        );

        CreateInterviewerRequest request = new CreateInterviewerRequest(
            "John Doe", "john@example.com", 5, slots
        );

        Interviewer result = interviewerService.createInterviewer(request);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(5, result.getMaxInterviewsPerWeek());
    }

    @Test
    void testCreateDuplicateInterviewer() {
        CreateInterviewerRequest request1 = new CreateInterviewerRequest(
            "John Doe", "john@example.com", 5, null
        );
        interviewerService.createInterviewer(request1);

        CreateInterviewerRequest request2 = new CreateInterviewerRequest(
            "Jane Doe", "john@example.com", 3, null
        );

        assertThrows(IllegalArgumentException.class, () -> {
            interviewerService.createInterviewer(request2);
        });
    }

    @Test
    void testGetInterviewer() {
        Interviewer interviewer = new Interviewer("Test User", "test@example.com", 3);
        interviewer = interviewerRepository.save(interviewer);

        Interviewer result = interviewerService.getInterviewer(interviewer.getId());

        assertNotNull(result);
        assertEquals(interviewer.getId(), result.getId());
        assertEquals("Test User", result.getName());
    }

    @Test
    void testGetInterviewerNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            interviewerService.getInterviewer(999L);
        });
    }

    @Test
    void testGetAllInterviewers() {
        Interviewer interviewer1 = new Interviewer("User1", "user1@example.com", 3);
        Interviewer interviewer2 = new Interviewer("User2", "user2@example.com", 5);
        
        interviewerRepository.save(interviewer1);
        interviewerRepository.save(interviewer2);

        List<Interviewer> result = interviewerService.getAllInterviewers();

        assertNotNull(result);
        assertTrue(result.size() >= 2);
    }

    @Test
    void testUpdateInterviewer() {
        Interviewer interviewer = new Interviewer("Original Name", "original@example.com", 3);
        interviewer = interviewerRepository.save(interviewer);

        List<AvailabilitySlotDto> newSlots = Arrays.asList(
            new AvailabilitySlotDto(DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0))
        );

        CreateInterviewerRequest updateRequest = new CreateInterviewerRequest(
            "Updated Name", "original@example.com", 7, newSlots
        );

        Interviewer result = interviewerService.updateInterviewer(interviewer.getId(), updateRequest);

        assertEquals("Updated Name", result.getName());
        assertEquals(7, result.getMaxInterviewsPerWeek());
    }
}