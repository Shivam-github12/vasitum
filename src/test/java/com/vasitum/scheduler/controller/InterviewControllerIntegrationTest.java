package com.vasitum.scheduler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasitum.scheduler.dto.AvailabilitySlotDto;
import com.vasitum.scheduler.dto.BookSlotRequest;
import com.vasitum.scheduler.dto.CreateInterviewerRequest;
import com.vasitum.scheduler.entity.InterviewSlot;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.repository.InterviewSlotRepository;
import com.vasitum.scheduler.repository.InterviewerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InterviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Autowired
    private InterviewSlotRepository interviewSlotRepository;

    private Interviewer testInterviewer;
    private InterviewSlot testSlot;

    @BeforeEach
    void setUp() {
        testInterviewer = new Interviewer("John Doe", "john@example.com", 5);
        testInterviewer = interviewerRepository.save(testInterviewer);

        testSlot = new InterviewSlot(
            testInterviewer,
            LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0),
            LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0)
        );
        testSlot = interviewSlotRepository.save(testSlot);
    }

    @Test
    void testCreateInterviewer() throws Exception {
        CreateInterviewerRequest request = new CreateInterviewerRequest(
            "Jane Smith",
            "jane@example.com",
            3,
            Arrays.asList(
                new AvailabilitySlotDto(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
            )
        );

        mockMvc.perform(post("/api/v1/interviewers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.maxInterviewsPerWeek").value(3));
    }

    @Test
    void testCreateInterviewerValidationError() throws Exception {
        CreateInterviewerRequest request = new CreateInterviewerRequest(
            "", // Invalid empty name
            "invalid-email", // Invalid email format
            -1, // Invalid negative number
            null
        );

        mockMvc.perform(post("/api/v1/interviewers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void testGetAvailableSlots() throws Exception {
        mockMvc.perform(get("/api/v1/interview-slots/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    void testBookSlot() throws Exception {
        BookSlotRequest request = new BookSlotRequest(
            testSlot.getId(),
            "Jane Smith",
            "jane@example.com"
        );

        mockMvc.perform(post("/api/v1/interview-slots/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateName").value("Jane Smith"))
                .andExpect(jsonPath("$.candidateEmail").value("jane@example.com"))
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    void testBookNonExistentSlot() throws Exception {
        BookSlotRequest request = new BookSlotRequest(
            999L,
            "Jane Smith",
            "jane@example.com"
        );

        mockMvc.perform(post("/api/v1/interview-slots/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void testGetInterviewer() throws Exception {
        mockMvc.perform(get("/api/v1/interviewers/{id}", testInterviewer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testInterviewer.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testGetNonExistentInterviewer() throws Exception {
        mockMvc.perform(get("/api/v1/interviewers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void testCancelSlot() throws Exception {
        // First book the slot
        testSlot.setStatus(InterviewSlot.SlotStatus.BOOKED);
        testSlot.setCandidateName("Test User");
        testSlot.setCandidateEmail("test@example.com");
        interviewSlotRepository.save(testSlot);

        // Then cancel it
        mockMvc.perform(delete("/api/v1/interview-slots/{slotId}/cancel", testSlot.getId()))
                .andExpect(status().isOk());
    }
}