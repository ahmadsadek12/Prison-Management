package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "schedule_days", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "day")
    private Set<String> days;

    @Column(nullable = false)
    private LocalTime start;

    @Column(nullable = false)
    private LocalTime end;

    public Schedule() {
        this.days = new HashSet<>();
    }

    public Schedule(Staff staff, Set<String> days, LocalTime start, LocalTime end) {
        this();
        setStaff(staff);
        setDays(days);
        setStart(start);
        setEnd(end);
    }

    public void setStaff(Staff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        this.staff = staff;
    }

    public void setDays(Set<String> days) {
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("Days cannot be null or empty");
        }
        Set<String> normalizedDays = new HashSet<>();
        for (String day : days) {
            String normalizedDay = day.trim().toUpperCase();
            if (!isValidDay(normalizedDay)) {
                throw new IllegalArgumentException("Invalid day: " + day + ". Must be one of: " + 
                    String.join(", ", Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")));
            }
            normalizedDays.add(normalizedDay);
        }
        this.days = normalizedDays;
    }

    public void setStart(LocalTime start) {
        if (start == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        this.start = start;
    }

    public void setEnd(LocalTime end) {
        if (end == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (start != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        this.end = end;
    }

    private boolean isValidDay(String day) {
        return Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
                     .contains(day);
    }

    public boolean hasDay(String day) {
        if (day == null || day.trim().isEmpty()) {
            return false;
        }
        return days.contains(day.trim().toUpperCase());
    }

    public boolean isWorkingDay(String day) {
        return hasDay(day);
    }

    public boolean isWeekend() {
        return hasDay("SATURDAY") || hasDay("SUNDAY");
    }

    public boolean isWeekday() {
        return hasDay("MONDAY") || hasDay("TUESDAY") || hasDay("WEDNESDAY") || 
               hasDay("THURSDAY") || hasDay("FRIDAY");
    }

    public boolean isFullTime() {
        return days.size() >= 5 && isWeekday();
    }

    public boolean isPartTime() {
        return days.size() < 5 || isWeekend();
    }

    public boolean isWorkingHours(LocalTime time) {
        if (time == null || start == null || end == null) {
            return false;
        }
        return !time.isBefore(start) && !time.isAfter(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return id.equals(schedule.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
