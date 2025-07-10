package org.example.services;

import org.example.models.Schedule;
import org.example.models.Staff;
import org.example.repositories.mysql.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(Integer id) {
        Assert.notNull(id, "Schedule ID cannot be null");
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByStaffId(Integer staffId) {
        Assert.notNull(staffId, "Staff ID cannot be null");
        return scheduleRepository.findByStaffId(staffId);
    }

    @Transactional
    public Schedule createSchedule(Staff staff, Set<String> days, LocalTime start, LocalTime end) {
        Assert.notNull(staff, "Staff cannot be null");
        Assert.notNull(days, "Days cannot be null or empty");
        Assert.notNull(start, "Start time cannot be null");
        Assert.notNull(end, "End time cannot be null");

        Schedule schedule = new Schedule(staff, days, start, end);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule updateSchedule(Integer id, Schedule updatedSchedule) {
        Assert.notNull(id, "Schedule ID cannot be null");
        Assert.notNull(updatedSchedule, "Updated schedule cannot be null");
        validateSchedule(updatedSchedule);

        Schedule existingSchedule = getScheduleById(id);
        updateScheduleFields(existingSchedule, updatedSchedule);
        return scheduleRepository.save(existingSchedule);
    }

    @Transactional
    public void deleteSchedule(Integer id) {
        Assert.notNull(id, "Schedule ID cannot be null");
        getScheduleById(id); // Verify exists
        scheduleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getCurrentSchedules(LocalTime currentTime) {
        Assert.notNull(currentTime, "Current time cannot be null");
        return scheduleRepository.findByEndAfter(currentTime);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getCurrentSchedulesByStaff(Integer staffId, LocalTime currentTime) {
        Assert.notNull(staffId, "Staff ID cannot be null");
        Assert.notNull(currentTime, "Current time cannot be null");
        return scheduleRepository.findByStaffIdAndEndAfter(staffId, currentTime);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByTimeRange(LocalTime startTime, LocalTime endTime) {
        Assert.notNull(startTime, "Start time cannot be null");
        Assert.notNull(endTime, "End time cannot be null");
        Assert.isTrue(!endTime.isBefore(startTime), "End time must not be before start time");
        return scheduleRepository.findByStartBetween(startTime, endTime);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByStaffAndTimeRange(Integer staffId, LocalTime startTime, LocalTime endTime) {
        Assert.notNull(staffId, "Staff ID cannot be null");
        Assert.notNull(startTime, "Start time cannot be null");
        Assert.notNull(endTime, "End time cannot be null");
        Assert.isTrue(!endTime.isBefore(startTime), "End time must not be before start time");
        return scheduleRepository.findByStaffIdAndStartBetween(staffId, startTime, endTime);
    }

    @Transactional(readOnly = true)
    public boolean isWorkingHours(Integer scheduleId, LocalTime time) {
        Assert.notNull(scheduleId, "Schedule ID cannot be null");
        Assert.notNull(time, "Time cannot be null");
        Schedule schedule = getScheduleById(scheduleId);
        return schedule.isWorkingHours(time);
    }

    @Transactional(readOnly = true)
    public boolean isWorkingDay(Integer scheduleId, String day) {
        Assert.notNull(scheduleId, "Schedule ID cannot be null");
        Assert.hasText(day, "Day cannot be null or empty");
        Schedule schedule = getScheduleById(scheduleId);
        return schedule.isWorkingDay(day);
    }

    private void validateSchedule(Schedule schedule) {
        Assert.notNull(schedule.getStaff(), "Staff cannot be null");
        Assert.notNull(schedule.getDays(), "Days cannot be null");
        Assert.isTrue(!schedule.getDays().isEmpty(), "Days cannot be empty");
        Assert.notNull(schedule.getStart(), "Start time cannot be null");
        Assert.notNull(schedule.getEnd(), "End time cannot be null");
        Assert.isTrue(!schedule.getEnd().isBefore(schedule.getStart()), "End time cannot be before start time");
    }

    private void updateScheduleFields(Schedule existingSchedule, Schedule updatedSchedule) {
        if (updatedSchedule.getStaff() != null) {
            existingSchedule.setStaff(updatedSchedule.getStaff());
        }
        if (updatedSchedule.getDays() != null && !updatedSchedule.getDays().isEmpty()) {
            existingSchedule.setDays(updatedSchedule.getDays());
        }
        if (updatedSchedule.getStart() != null) {
            existingSchedule.setStart(updatedSchedule.getStart());
        }
        if (updatedSchedule.getEnd() != null) {
            existingSchedule.setEnd(updatedSchedule.getEnd());
        }
    }
}
