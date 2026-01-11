package com.vasitum.scheduler.repository;

import com.vasitum.scheduler.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    
    List<AvailabilitySlot> findByInterviewerIdAndIsActiveTrue(Long interviewerId);
    
    @Query("SELECT a FROM AvailabilitySlot a WHERE a.interviewer.id = :interviewerId " +
           "AND a.dayOfWeek = :dayOfWeek AND a.isActive = true")
    List<AvailabilitySlot> findByInterviewerAndDayOfWeek(
        @Param("interviewerId") Long interviewerId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
    
    @Query("SELECT a FROM AvailabilitySlot a WHERE a.isActive = true")
    List<AvailabilitySlot> findAllActiveSlots();
}