package com.vasitum.scheduler.dto;

import com.vasitum.scheduler.entity.InterviewSlot;
import java.time.LocalDateTime;

public class InterviewSlotDto {
    private Long id;
    private String interviewerName;
    private String interviewerEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private InterviewSlot.SlotStatus status;
    private String candidateName;
    private String candidateEmail;
    private LocalDateTime bookedAt;

    // Constructors
    public InterviewSlotDto() {}

    public InterviewSlotDto(InterviewSlot slot) {
        this.id = slot.getId();
        this.interviewerName = slot.getInterviewer().getName();
        this.interviewerEmail = slot.getInterviewer().getEmail();
        this.startTime = slot.getStartTime();
        this.endTime = slot.getEndTime();
        this.status = slot.getStatus();
        this.candidateName = slot.getCandidateName();
        this.candidateEmail = slot.getCandidateEmail();
        this.bookedAt = slot.getBookedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public String getInterviewerEmail() { return interviewerEmail; }
    public void setInterviewerEmail(String interviewerEmail) { this.interviewerEmail = interviewerEmail; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public InterviewSlot.SlotStatus getStatus() { return status; }
    public void setStatus(InterviewSlot.SlotStatus status) { this.status = status; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
}