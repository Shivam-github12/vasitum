package com.vasitum.scheduler.repository;

import com.vasitum.scheduler.entity.InterviewSlot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InterviewSlot s WHERE s.id = :id")
    Optional<InterviewSlot> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT s FROM InterviewSlot s WHERE s.status = 'AVAILABLE' " +
           "AND s.startTime >= :startTime AND s.endTime <= :endTime " +
           "AND (:cursor IS NULL OR s.id > :cursor) " +
           "ORDER BY s.id ASC")
    List<InterviewSlot> findAvailableSlotsCursor(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("cursor") Long cursor,
        Pageable pageable
    );
    
    @Query("SELECT s FROM InterviewSlot s WHERE s.status = 'AVAILABLE' " +
           "AND s.startTime >= :startTime AND s.endTime <= :endTime " +
           "ORDER BY s.startTime ASC")
    List<InterviewSlot> findAvailableSlots(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );
    
    @Query("SELECT s FROM InterviewSlot s WHERE s.interviewer.id = :interviewerId " +
           "AND s.startTime >= :weekStart AND s.startTime < :weekEnd " +
           "AND s.status = 'BOOKED'")
    List<InterviewSlot> findBookedSlotsForWeek(
        @Param("interviewerId") Long interviewerId,
        @Param("weekStart") LocalDateTime weekStart,
        @Param("weekEnd") LocalDateTime weekEnd
    );
    
    @Modifying
    @Query("UPDATE InterviewSlot s SET s.status = 'BOOKED', s.candidateName = :candidateName, " +
           "s.candidateEmail = :candidateEmail, s.bookedAt = :bookedAt " +
           "WHERE s.id = :slotId AND s.status = 'AVAILABLE'")
    int bookSlot(@Param("slotId") Long slotId, 
                 @Param("candidateName") String candidateName,
                 @Param("candidateEmail") String candidateEmail,
                 @Param("bookedAt") LocalDateTime bookedAt);
    
    List<InterviewSlot> findByInterviewerIdAndStartTimeBetween(
        Long interviewerId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
}