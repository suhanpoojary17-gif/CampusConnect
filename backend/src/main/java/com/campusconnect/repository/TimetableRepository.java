package com.campusconnect.repository;

import com.campusconnect.model.DayOfWeek;
import com.campusconnect.model.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    List<Timetable> findBySectionIdAndDayOrderByStartTime(
            UUID sectionId,
            DayOfWeek day
    );

    List<Timetable> findBySectionIdOrderByDayAscStartTimeAsc(
            UUID sectionId
    );

    List<Timetable> findByTeacherIdOrderByDayAscStartTimeAsc(
            Long teacherId
    );

    // --- Section Overlap Checks ---

    @Query("""
        SELECT COUNT(t) > 0
        FROM Timetable t
        WHERE t.section.id = :sectionId
          AND t.day = :day
          AND t.startTime < :endTime
          AND t.endTime > :startTime
    """)
    boolean existsConflict(
            @Param("sectionId") UUID sectionId,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
        SELECT COUNT(t) > 0
        FROM Timetable t
        WHERE t.section.id = :sectionId
          AND t.day = :day
          AND t.startTime < :endTime
          AND t.endTime > :startTime
          AND t.id <> :id
    """)
    boolean existsConflictExceptId(
            @Param("sectionId") UUID sectionId,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("id") Long id
    );

    // --- Teacher Overlap Checks ---

    @Query("""
        SELECT COUNT(t) > 0
        FROM Timetable t
        WHERE t.teacher.id = :teacherId
          AND t.day = :day
          AND t.startTime < :endTime
          AND t.endTime > :startTime
    """)
    boolean existsTeacherConflict(
            @Param("teacherId") Long teacherId,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
        SELECT COUNT(t) > 0
        FROM Timetable t
        WHERE t.teacher.id = :teacherId
          AND t.day = :day
          AND t.startTime < :endTime
          AND t.endTime > :startTime
          AND t.id <> :id
    """)
    boolean existsTeacherConflictExceptId(
            @Param("teacherId") Long teacherId,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("id") Long id
    );

    // --- Room Overlap Checks ---

    @Query("""
        SELECT COUNT(t) > 0
        FROM Timetable t
        WHERE t.room = :room
          AND t.day = :day
          AND t.startTime < :endTime
          AND t.endTime > :startTime
    """)
    boolean existsRoomConflict(
            @Param("room") String room,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
        SELECT COUNT(t) > 0
        FROM Timetable t
        WHERE t.room = :room
          AND t.day = :day
          AND t.startTime < :endTime
          AND t.endTime > :startTime
          AND t.id <> :id
    """)
    boolean existsRoomConflictExceptId(
            @Param("room") String room,
            @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("id") Long id
    );
}