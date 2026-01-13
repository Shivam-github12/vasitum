package com.vasitum.scheduler.controller;

import com.vasitum.scheduler.dto.PaginatedResponse;
import com.vasitum.scheduler.dto.InterviewSlotDto;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.service.InterviewSlotService;
import com.vasitum.scheduler.service.InterviewerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private InterviewSlotService interviewSlotService;

    @Autowired
    private InterviewerService interviewerService;

    @GetMapping("/")
    public String index(Model model, @RequestParam(required = false) String cursor) {
        PaginatedResponse<InterviewSlotDto> slots = interviewSlotService.getAvailableSlots(cursor, 10);
        List<Interviewer> interviewers = interviewerService.getAllInterviewers();
        
        model.addAttribute("slots", slots);
        model.addAttribute("interviewers", interviewers);
        return "index";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        List<Interviewer> interviewers = interviewerService.getAllInterviewers();
        model.addAttribute("interviewers", interviewers);
        return "admin";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "notifications";
    }

    @GetMapping("/booked")
    public String bookedSlots(Model model) {
        try {
            List<Interviewer> interviewers = interviewerService.getAllInterviewers();
            List<InterviewSlotDto> bookedSlots = new ArrayList<>();
            
            for (Interviewer interviewer : interviewers) {
                List<InterviewSlotDto> slots = interviewSlotService.getSlotsByInterviewer(interviewer.getId());
                for (InterviewSlotDto slot : slots) {
                    if ("BOOKED".equals(slot.getStatus().toString())) {
                        bookedSlots.add(slot);
                    }
                }
            }
            
            model.addAttribute("bookedSlots", bookedSlots);
            return "booked";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading booked slots: " + e.getMessage());
            return "booked";
        }
    }

    @GetMapping("/book")
    public String bookSlot(@RequestParam Long slotId, Model model) {
        try {
            InterviewSlotDto slot = interviewSlotService.getSlot(slotId);
            model.addAttribute("slot", slot);
            return "book-slot";
        } catch (Exception e) {
            return "redirect:/?error=slot-not-found";
        }
    }

    @GetMapping("/debug")
    public String debug(Model model) {
        List<Interviewer> interviewers = interviewerService.getAllInterviewers();
        model.addAttribute("interviewers", interviewers);
        return "debug";
    }
}