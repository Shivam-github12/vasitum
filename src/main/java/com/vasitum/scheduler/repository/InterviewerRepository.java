package com.vasitum.scheduler.repository;

import com.vasitum.scheduler.entity.Interviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {
    
    Optional<Interviewer> findByEmail(String email);
    
    @Query("SELECT i FROM Interviewer i WHERE i.id = :interviewerId AND " +
           "(SELECT COUNT(s) FROM InterviewSlot s WHERE s.interviewer.id = :interviewerId " +
           "AND s.status = 'BOOKED' AND s.startTime >= :weekStart AND s.startTime < :weekEnd) < i.maxInterviewsPerWeek")
    Optional<Interviewer> findInterviewerWithAvailableCapacity(
        @Param("interviewerId") Long interviewerId,
        @Param("weekStart") LocalDateTime weekStart,
        @Param("weekEnd") LocalDateTime weekEnd
    );
    
    @Query("SELECT i FROM Interviewer i JOIN i.availabilitySlots a WHERE a.isActive = true")
    List<Interviewer> findInterviewersWithActiveSlots();
}