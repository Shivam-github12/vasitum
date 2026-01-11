package com.vasitum.scheduler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookSlotRequest {
    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    @NotBlank(message = "Candidate email is required")
    @Email(message = "Invalid email format")
    private String candidateEmail;

    // Constructors
    public BookSlotRequest() {}

    public BookSlotRequest(Long slotId, String candidateName, String candidateEmail) {
        this.slotId = slotId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
    }

    // Getters and Setters
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }
}