package com.vasitum.scheduler.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public class CreateInterviewerRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Max interviews per week is required")
    @Positive(message = "Max interviews per week must be positive")
    private Integer maxInterviewsPerWeek;

    @Valid
    private List<AvailabilitySlotDto> availabilitySlots;

    // Constructors
    public CreateInterviewerRequest() {}

    public CreateInterviewerRequest(String name, String email, Integer maxInterviewsPerWeek, List<AvailabilitySlotDto> availabilitySlots) {
        this.name = name;
        this.email = email;
        this.maxInterviewsPerWeek = maxInterviewsPerWeek;
        this.availabilitySlots = availabilitySlots;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getMaxInterviewsPerWeek() { return maxInterviewsPerWeek; }
    public void setMaxInterviewsPerWeek(Integer maxInterviewsPerWeek) { this.maxInterviewsPerWeek = maxInterviewsPerWeek; }

    public List<AvailabilitySlotDto> getAvailabilitySlots() { return availabilitySlots; }
    public void setAvailabilitySlots(List<AvailabilitySlotDto> availabilitySlots) { this.availabilitySlots = availabilitySlots; }
}