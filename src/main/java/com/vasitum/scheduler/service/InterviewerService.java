package com.vasitum.scheduler.service;

import com.vasitum.scheduler.dto.AvailabilitySlotDto;
import com.vasitum.scheduler.dto.CreateInterviewerRequest;
import com.vasitum.scheduler.entity.AvailabilitySlot;
import com.vasitum.scheduler.entity.InterviewSlot;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.exception.ResourceNotFoundException;
import com.vasitum.scheduler.repository.AvailabilitySlotRepository;
import com.vasitum.scheduler.repository.InterviewSlotRepository;
import com.vasitum.scheduler.repository.InterviewerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InterviewerService {

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    private InterviewSlotRepository interviewSlotRepository;

    public Interviewer createInterviewer(CreateInterviewerRequest request) {
        // Check if interviewer already exists
        if (interviewerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Interviewer with email " + request.getEmail() + " already exists");
        }

        // Create interviewer
        Interviewer interviewer = new Interviewer(
            request.getName(),
            request.getEmail(),
            request.getMaxInterviewsPerWeek()
        );
        interviewer = interviewerRepository.save(interviewer);

        // Create availability slots
        if (request.getAvailabilitySlots() != null) {
            for (AvailabilitySlotDto slotDto : request.getAvailabilitySlots()) {
                AvailabilitySlot slot = new AvailabilitySlot(
                    interviewer,
                    slotDto.getDayOfWeek(),
                    slotDto.getStartTime(),
                    slotDto.getEndTime()
                );
                availabilitySlotRepository.save(slot);
            }
        }

        // Generate interview slots for next 2 weeks
        generateInterviewSlots(interviewer.getId());

        return interviewer;
    }

    public void generateInterviewSlots(Long interviewerId) {
        Interviewer interviewer = interviewerRepository.findById(interviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("Interviewer not found"));

        List<AvailabilitySlot> availabilitySlots = availabilitySlotRepository
            .findByInterviewerIdAndIsActiveTrue(interviewerId);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2);

        List<InterviewSlot> slotsToCreate = new ArrayList<>();

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            
            for (AvailabilitySlot availabilitySlot : availabilitySlots) {
                if (availabilitySlot.getDayOfWeek().equals(dayOfWeek)) {
                    // Generate 1-hour slots
                    LocalTime currentTime = availabilitySlot.getStartTime();
                    while (currentTime.isBefore(availabilitySlot.getEndTime())) {
                        LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
                        LocalDateTime slotEnd = slotStart.plusHours(1);

                        // Check if slot already exists
                        List<InterviewSlot> existingSlots = interviewSlotRepository
                            .findByInterviewerIdAndStartTimeBetween(
                                interviewerId, slotStart, slotEnd
                            );

                        if (existingSlots.isEmpty()) {
                            InterviewSlot slot = new InterviewSlot(interviewer, slotStart, slotEnd);
                            slotsToCreate.add(slot);
                        }

                        currentTime = currentTime.plusHours(1);
                    }
                }
            }
        }

        if (!slotsToCreate.isEmpty()) {
            interviewSlotRepository.saveAll(slotsToCreate);
        }
    }

    @Transactional(readOnly = true)
    public Interviewer getInterviewer(Long id) {
        return interviewerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Interviewer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Interviewer> getAllInterviewers() {
        return interviewerRepository.findAll();
    }

    public Interviewer updateInterviewer(Long id, CreateInterviewerRequest request) {
        Interviewer interviewer = getInterviewer(id);
        
        interviewer.setName(request.getName());
        interviewer.setMaxInterviewsPerWeek(request.getMaxInterviewsPerWeek());
        
        // Update availability slots if provided
        if (request.getAvailabilitySlots() != null) {
            // Deactivate existing slots
            List<AvailabilitySlot> existingSlots = availabilitySlotRepository
                .findByInterviewerIdAndIsActiveTrue(id);
            existingSlots.forEach(slot -> slot.setIsActive(false));
            availabilitySlotRepository.saveAll(existingSlots);

            // Create new slots
            for (AvailabilitySlotDto slotDto : request.getAvailabilitySlots()) {
                AvailabilitySlot slot = new AvailabilitySlot(
                    interviewer,
                    slotDto.getDayOfWeek(),
                    slotDto.getStartTime(),
                    slotDto.getEndTime()
                );
                availabilitySlotRepository.save(slot);
            }

            // Regenerate interview slots
            generateInterviewSlots(id);
        }

        return interviewerRepository.save(interviewer);
    }
}