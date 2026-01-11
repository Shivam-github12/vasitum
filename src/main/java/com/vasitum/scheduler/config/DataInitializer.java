package com.vasitum.scheduler.config;

import com.vasitum.scheduler.entity.AvailabilitySlot;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.repository.AvailabilitySlotRepository;
import com.vasitum.scheduler.repository.InterviewerRepository;
import com.vasitum.scheduler.service.InterviewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Component
@Profile("never")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private InterviewerRepository interviewerRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    private InterviewerService interviewerService;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (interviewerRepository.count() > 0) {
            return;
        }

        // Create sample interviewers
        createSampleInterviewer("John Smith", "john.smith@company.com", 5);
        createSampleInterviewer("Sarah Johnson", "sarah.johnson@company.com", 3);
        createSampleInterviewer("Mike Wilson", "mike.wilson@company.com", 4);
    }

    private void createSampleInterviewer(String name, String email, int maxInterviews) {
        Interviewer interviewer = new Interviewer(name, email, maxInterviews);
        interviewer = interviewerRepository.save(interviewer);

        // Add availability slots (Monday to Friday, 9 AM to 5 PM)
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, 
                                            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            AvailabilitySlot slot = new AvailabilitySlot(
                interviewer, day, LocalTime.of(9, 0), LocalTime.of(17, 0)
            );
            availabilitySlotRepository.save(slot);
        }

        // Generate interview slots
        interviewerService.generateInterviewSlots(interviewer.getId());
    }
}