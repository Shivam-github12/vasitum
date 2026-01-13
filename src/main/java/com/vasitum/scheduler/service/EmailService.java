package com.vasitum.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${app.mail.from:noreply@interviewscheduler.com}")
    private String fromEmail;
    
    @Value("${app.mail.enabled:false}")
    private boolean emailEnabled;
    
    public boolean sendEmail(String to, String subject, String content) {
        if (!emailEnabled || mailSender == null) {
            logger.info("Email sending is disabled or mailSender not available. Would send email to: {} with subject: {}", to, subject);
            return true; // Return true for testing purposes
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
            return false;
        }
    }
    
    public String generateBookingConfirmationEmail(String candidateName, String interviewerName, 
                                                 String interviewDate, String interviewTime) {
        return String.format("""
            Dear %s,
            
            Your interview has been successfully scheduled!
            
            Interview Details:
            - Interviewer: %s
            - Date: %s
            - Time: %s
            
            Please make sure to join the interview on time. If you need to reschedule or cancel, 
            please contact us as soon as possible.
            
            Best regards,
            Interview Scheduler Team
            """, candidateName, interviewerName, interviewDate, interviewTime);
    }
    
    public String generateInterviewReminderEmail(String candidateName, String interviewerName,
                                               String interviewDate, String interviewTime) {
        return String.format("""
            Dear %s,
            
            This is a friendly reminder about your upcoming interview scheduled for tomorrow.
            
            Interview Details:
            - Interviewer: %s
            - Date: %s
            - Time: %s
            
            Please make sure you are prepared and join on time.
            
            Best regards,
            Interview Scheduler Team
            """, candidateName, interviewerName, interviewDate, interviewTime);
    }
    
    public String generateCancellationEmail(String candidateName, String interviewerName,
                                          String interviewDate, String interviewTime) {
        return String.format("""
            Dear %s,
            
            We regret to inform you that your interview has been cancelled.
            
            Cancelled Interview Details:
            - Interviewer: %s
            - Date: %s
            - Time: %s
            
            Please feel free to book another available slot at your convenience.
            
            Best regards,
            Interview Scheduler Team
            """, candidateName, interviewerName, interviewDate, interviewTime);
    }
    
    public String generateSlotGenerationAlert(String interviewerName, int slotsGenerated) {
        return String.format("""
            Dear %s,
            
            New interview slots have been generated for you.
            
            Details:
            - Number of new slots: %d
            - Generated for the next 2 weeks
            
            You can view and manage your slots through the admin panel.
            
            Best regards,
            Interview Scheduler Team
            """, interviewerName, slotsGenerated);
    }
}