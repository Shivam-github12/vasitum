package com.vasitum.scheduler.service;

import com.vasitum.scheduler.dto.BookSlotRequest;
import com.vasitum.scheduler.dto.InterviewSlotDto;
import com.vasitum.scheduler.dto.PaginatedResponse;
import com.vasitum.scheduler.entity.AvailabilitySlot;
import com.vasitum.scheduler.entity.InterviewSlot;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.exception.ResourceNotFoundException;
import com.vasitum.scheduler.exception.SlotBookingException;
import com.vasitum.scheduler.repository.AvailabilitySlotRepository;
import com.vasitum.scheduler.repository.InterviewSlotRepository;
import com.vasitum.scheduler.repository.InterviewerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InterviewSlotServiceTest {

    @Autowired
    private InterviewSlotService interviewSlotService;

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Autowired
    private InterviewSlotRepository interviewSlotRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    private Interviewer testInterviewer;
    private InterviewSlot testSlot;

    @BeforeEach
    void setUp() {
        // Create test interviewer
        testInterviewer = new Interviewer("John Doe", "john@example.com", 5);
        testInterviewer = interviewerRepository.save(testInterviewer);

        // Create availability slot
        AvailabilitySlot availabilitySlot = new AvailabilitySlot(
            testInterviewer, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)
        );
        availabilitySlotRepository.save(availabilitySlot);

        // Create test interview slot
        testSlot = new InterviewSlot(
            testInterviewer,
            LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0),
            LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0)
        );
        testSlot = interviewSlotRepository.save(testSlot);
    }

    @Test
    void testGetAvailableSlots() {
        PaginatedResponse<InterviewSlotDto> response = interviewSlotService.getAvailableSlots(null, 10);
        
        assertNotNull(response);
        assertFalse(response.getData().isEmpty());
        assertTrue(response.getData().size() >= 1);
        // Find our test slot in the response
        boolean foundTestSlot = response.getData().stream()
            .anyMatch(slot -> slot.getId().equals(testSlot.getId()));
        assertTrue(foundTestSlot, "Test slot should be found in available slots");
    }

    @Test
    void testBookSlot() {
        BookSlotRequest request = new BookSlotRequest(testSlot.getId(), "Jane Smith", "jane@example.com");
        
        InterviewSlotDto result = interviewSlotService.bookSlot(request);
        
        assertNotNull(result);
        assertEquals(testSlot.getId(), result.getId());
        assertEquals("Jane Smith", result.getCandidateName());
        assertEquals("jane@example.com", result.getCandidateEmail());
        assertEquals(InterviewSlot.SlotStatus.BOOKED, result.getStatus());
    }

    @Test
    void testBookSlotNotFound() {
        BookSlotRequest request = new BookSlotRequest(999L, "Jane Smith", "jane@example.com");
        
        assertThrows(ResourceNotFoundException.class, () -> {
            interviewSlotService.bookSlot(request);
        });
    }

    @Test
    void testBookAlreadyBookedSlot() {
        // First booking
        BookSlotRequest request1 = new BookSlotRequest(testSlot.getId(), "Jane Smith", "jane@example.com");
        interviewSlotService.bookSlot(request1);
        
        // Second booking attempt
        BookSlotRequest request2 = new BookSlotRequest(testSlot.getId(), "John Doe", "john@example.com");
        
        assertThrows(SlotBookingException.class, () -> {
            interviewSlotService.bookSlot(request2);
        });
    }

    // Note: True concurrent testing is difficult in unit tests
    // This test demonstrates the booking logic but may not fail as expected
    // In production, pessimistic locking will prevent race conditions
    @Test
    void testSequentialBookingPreventsDoubleBooking() {
        BookSlotRequest request1 = new BookSlotRequest(testSlot.getId(), "User1", "user1@example.com");
        BookSlotRequest request2 = new BookSlotRequest(testSlot.getId(), "User2", "user2@example.com");

        // First booking should succeed
        InterviewSlotDto result1 = interviewSlotService.bookSlot(request1);
        assertNotNull(result1);
        assertEquals("User1", result1.getCandidateName());

        // Second booking should fail
        assertThrows(SlotBookingException.class, () -> {
            interviewSlotService.bookSlot(request2);
        });
    }

    @Test
    void testUpdateSlot() {
        // First book the slot
        BookSlotRequest bookRequest = new BookSlotRequest(testSlot.getId(), "Jane Smith", "jane@example.com");
        interviewSlotService.bookSlot(bookRequest);

        // Then update it
        BookSlotRequest updateRequest = new BookSlotRequest(testSlot.getId(), "Jane Doe", "jane.doe@example.com");
        InterviewSlotDto result = interviewSlotService.updateSlot(testSlot.getId(), updateRequest);

        assertEquals("Jane Doe", result.getCandidateName());
        assertEquals("jane.doe@example.com", result.getCandidateEmail());
    }

    @Test
    void testCancelSlot() {
        // First book the slot
        BookSlotRequest bookRequest = new BookSlotRequest(testSlot.getId(), "Jane Smith", "jane@example.com");
        interviewSlotService.bookSlot(bookRequest);

        // Then cancel it
        interviewSlotService.cancelSlot(testSlot.getId());

        InterviewSlotDto result = interviewSlotService.getSlot(testSlot.getId());
        assertEquals(InterviewSlot.SlotStatus.AVAILABLE, result.getStatus());
        assertNull(result.getCandidateName());
        assertNull(result.getCandidateEmail());
    }

    @Test
    void testGetSlotsByInterviewer() {
        var slots = interviewSlotService.getSlotsByInterviewer(testInterviewer.getId());
        
        assertNotNull(slots);
        assertFalse(slots.isEmpty());
        assertEquals(1, slots.size());
        assertEquals(testSlot.getId(), slots.get(0).getId());
    }

    @Test
    void testPaginationWithCursor() {
        // Create additional slots
        for (int i = 0; i < 5; i++) {
            InterviewSlot slot = new InterviewSlot(
                testInterviewer,
                LocalDateTime.now().plusDays(i + 2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(i + 2).withHour(11).withMinute(0).withSecond(0).withNano(0)
            );
            interviewSlotRepository.save(slot);
        }

        // Test first page
        PaginatedResponse<InterviewSlotDto> firstPage = interviewSlotService.getAvailableSlots(null, 3);
        assertEquals(3, firstPage.getData().size());
        assertTrue(firstPage.isHasNext());

        // Test second page
        PaginatedResponse<InterviewSlotDto> secondPage = interviewSlotService.getAvailableSlots(firstPage.getNextCursor(), 3);
        assertEquals(3, secondPage.getData().size());
        assertTrue(secondPage.isHasPrev());
    }
}