package com.vasitum.scheduler.service;

import com.vasitum.scheduler.dto.BookSlotRequest;
import com.vasitum.scheduler.dto.InterviewSlotDto;
import com.vasitum.scheduler.dto.PaginatedResponse;
import com.vasitum.scheduler.entity.InterviewSlot;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.exception.ResourceNotFoundException;
import com.vasitum.scheduler.exception.SlotBookingException;
import com.vasitum.scheduler.repository.InterviewSlotRepository;
import com.vasitum.scheduler.repository.InterviewerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewSlotService {

    @Autowired
    private InterviewSlotRepository interviewSlotRepository;

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Transactional(readOnly = true)
    public PaginatedResponse<InterviewSlotDto> getAvailableSlots(String cursor, int limit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoWeeksFromNow = now.plusWeeks(2);
        
        Long cursorId = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, limit + 1); // Get one extra to check if there's a next page
        
        List<InterviewSlot> slots = interviewSlotRepository.findAvailableSlotsCursor(
            now, twoWeeksFromNow, cursorId, pageable
        );
        
        boolean hasNext = slots.size() > limit;
        if (hasNext) {
            slots = slots.subList(0, limit);
        }
        
        List<InterviewSlotDto> slotDtos = slots.stream()
            .map(InterviewSlotDto::new)
            .collect(Collectors.toList());
        
        String nextCursor = hasNext && !slots.isEmpty() ? 
            encodeCursor(slots.get(slots.size() - 1).getId()) : null;
        String prevCursor = cursorId != null ? encodeCursor(cursorId) : null;
        
        return new PaginatedResponse<>(
            slotDtos, 
            nextCursor, 
            prevCursor, 
            hasNext, 
            cursorId != null, 
            slotDtos.size()
        );
    }

    public InterviewSlotDto bookSlot(BookSlotRequest request) {
        // Use pessimistic locking to prevent race conditions
        InterviewSlot slot = interviewSlotRepository.findByIdWithLock(request.getSlotId())
            .orElseThrow(() -> new ResourceNotFoundException("Interview slot not found"));

        // Check if slot is still available
        if (slot.getStatus() != InterviewSlot.SlotStatus.AVAILABLE) {
            throw new SlotBookingException("Slot is no longer available");
        }

        // Check if slot is in the future
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new SlotBookingException("Cannot book past slots");
        }

        // Check interviewer's weekly capacity
        LocalDateTime weekStart = slot.getStartTime().truncatedTo(ChronoUnit.DAYS)
            .with(java.time.DayOfWeek.MONDAY);
        LocalDateTime weekEnd = weekStart.plusWeeks(1);

        List<InterviewSlot> bookedSlotsThisWeek = interviewSlotRepository
            .findBookedSlotsForWeek(slot.getInterviewer().getId(), weekStart, weekEnd);

        if (bookedSlotsThisWeek.size() >= slot.getInterviewer().getMaxInterviewsPerWeek()) {
            throw new SlotBookingException("Interviewer has reached maximum interviews for this week");
        }

        // Book the slot
        slot.setStatus(InterviewSlot.SlotStatus.BOOKED);
        slot.setCandidateName(request.getCandidateName());
        slot.setCandidateEmail(request.getCandidateEmail());
        slot.setBookedAt(LocalDateTime.now());

        slot = interviewSlotRepository.save(slot);
        return new InterviewSlotDto(slot);
    }

    public InterviewSlotDto updateSlot(Long slotId, BookSlotRequest request) {
        InterviewSlot slot = interviewSlotRepository.findByIdWithLock(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Interview slot not found"));

        if (slot.getStatus() != InterviewSlot.SlotStatus.BOOKED) {
            throw new SlotBookingException("Only booked slots can be updated");
        }

        slot.setCandidateName(request.getCandidateName());
        slot.setCandidateEmail(request.getCandidateEmail());

        slot = interviewSlotRepository.save(slot);
        return new InterviewSlotDto(slot);
    }

    public void cancelSlot(Long slotId) {
        InterviewSlot slot = interviewSlotRepository.findByIdWithLock(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Interview slot not found"));

        if (slot.getStatus() != InterviewSlot.SlotStatus.BOOKED) {
            throw new SlotBookingException("Only booked slots can be cancelled");
        }

        slot.setStatus(InterviewSlot.SlotStatus.AVAILABLE);
        slot.setCandidateName(null);
        slot.setCandidateEmail(null);
        slot.setBookedAt(null);

        interviewSlotRepository.save(slot);
    }

    @Transactional(readOnly = true)
    public InterviewSlotDto getSlot(Long slotId) {
        InterviewSlot slot = interviewSlotRepository.findById(slotId)
            .orElseThrow(() -> new ResourceNotFoundException("Interview slot not found"));
        return new InterviewSlotDto(slot);
    }

    @Transactional(readOnly = true)
    public List<InterviewSlotDto> getSlotsByInterviewer(Long interviewerId) {
        Interviewer interviewer = interviewerRepository.findById(interviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("Interviewer not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoWeeksFromNow = now.plusWeeks(2);

        List<InterviewSlot> slots = interviewSlotRepository
            .findByInterviewerIdAndStartTimeBetween(interviewerId, now, twoWeeksFromNow);

        return slots.stream()
            .map(InterviewSlotDto::new)
            .collect(Collectors.toList());
    }

    private String encodeCursor(Long id) {
        if (id == null) return null;
        return Base64.getEncoder().encodeToString(id.toString().getBytes());
    }

    private Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Long.parseLong(decoded);
        } catch (Exception e) {
            return null;
        }
    }
}