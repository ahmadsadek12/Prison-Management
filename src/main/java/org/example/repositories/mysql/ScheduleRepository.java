package org.example.repositories.mysql;

import org.example.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    
    // Basic queries
    List<Schedule> findByStaffId(Integer staffId);
    
    // Time-based queries
    List<Schedule> findByStart(LocalTime startTime);
    List<Schedule> findByEnd(LocalTime endTime);
    List<Schedule> findByStartBetween(LocalTime startTime, LocalTime endTime);
    List<Schedule> findByEndBetween(LocalTime startTime, LocalTime endTime);
    
    // Staff and time combinations
    List<Schedule> findByStaffIdAndStart(Integer staffId, LocalTime startTime);
    List<Schedule> findByStaffIdAndEnd(Integer staffId, LocalTime endTime);
    List<Schedule> findByStaffIdAndStartBetween(Integer staffId, LocalTime startTime, LocalTime endTime);
    List<Schedule> findByStaffIdAndEndBetween(Integer staffId, LocalTime startTime, LocalTime endTime);
    
    // Find current schedules (where end time is in the future)
    List<Schedule> findByEndAfter(LocalTime currentTime);
    
    // Find current schedules by staff
    List<Schedule> findByStaffIdAndEndAfter(Integer staffId, LocalTime currentTime);
    
    // Find schedules that overlap with a given time range
    List<Schedule> findByStartBeforeAndEndAfter(LocalTime startTime, LocalTime endTime);
    
    // Find schedules by staff that overlap with a given time range
    List<Schedule> findByStaffIdAndStartBeforeAndEndAfter(
        Integer staffId, LocalTime startTime, LocalTime endTime);
    
    // Find schedules by start time
    List<Schedule> findByStartBefore(LocalDateTime time);
    
    // Find schedules by start time after
    List<Schedule> findByStartAfter(LocalDateTime time);
    
    // Find schedules by start time between
    List<Schedule> findByStartBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // Find schedules by staff ID and start time
    List<Schedule> findByStaffIdAndStartBefore(Integer staffId, LocalDateTime time);
    
    // Find schedules by staff ID and start time after
    List<Schedule> findByStaffIdAndStartAfter(Integer staffId, LocalDateTime time);
    
    // Find schedules by staff ID and start time between
    List<Schedule> findByStaffIdAndStartBetween(
        Integer staffId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find schedules by staff ID and end time
    List<Schedule> findByStaffIdAndEndBefore(Integer staffId, LocalDateTime time);
    
    // Find schedules by staff ID and end time after
    List<Schedule> findByStaffIdAndEndAfter(Integer staffId, LocalDateTime time);
    
    // Find schedules by staff ID and end time between
    List<Schedule> findByStaffIdAndEndBetween(
        Integer staffId, LocalDateTime startTime, LocalDateTime endTime);
}
