package refactor.baseline;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import refactor.common.Course;
import refactor.common.EnrollmentRecord;
import refactor.common.Student;
import refactor.common.exception.CourseFullException;
import refactor.common.exception.EnrollmentConflictException;
import refactor.common.exception.NotEnrolledException;

/**
 * Service for managing student enrollment records.
 * Uses a fixed enrollment capacity of 30 per course.
 * This service is not thread-safe.
 *
 * @author Vax4Cat
 */
public class EnrollSvc {
    private static final int DEFAULT_CAPACITY = 30;
    List<EnrollmentRecord> enrollmentRecords = new ArrayList<>();

    /**
     * Validate enrollment parameters.
     *
     * @param course  the course to validate
     * @param student the student to validate
     * @throws IllegalArgumentException if student or course is null
     * @requires true
     * @modifies nothing
     * @effects validate student and course parameters
     */
    private static void validateParameters(Course course, Student student) {
        if (student == null) {
            throw new IllegalArgumentException("null");
        }
        if (course == null) {
            throw new IllegalArgumentException("null");
        }
    }

    /**
     * Enroll a student in a course.
     *
     * @param student the student to enroll
     * @param course  the course to enroll in
     * @throws IllegalArgumentException    if student or course is null
     * @throws EnrollmentConflictException if the student is already enrolled
     * @throws CourseFullException         if the course has reached the fixed capacity
     * @requires student and course are non-null for successful enrollment
     * @modifies enrollmentRecords, System.out
     * @effects add an enrollment record and print "ok" on success;
     * leave enrollment records unchanged on validation failure
     */
    public void add(Student student, Course course) {
        validateParameters(course, student);

        if (isEnrolled(student, course)) {
            throw new EnrollmentConflictException("conflict");
        }

        validateCapacity(course);
        LocalDateTime enrollmentTime = LocalDateTime.now();
        enrollmentRecords.add(new EnrollmentRecord(student, course, enrollmentTime));
        System.out.println("ok");
    }

    /**
     * Validate the enrollment capacity of a course.
     *
     * @param course the course whose capacity is checked
     * @throws CourseFullException if the course is full
     * @requires course != null
     * @modifies nothing
     * @effects check whether the fixed capacity has been reached
     */
    private void validateCapacity(Course course) {
        int enrollmentCount = 0;
        for (var record : enrollmentRecords) {
            if (record.course().equals(course)) enrollmentCount++;
        }
        if (enrollmentCount >= DEFAULT_CAPACITY) {
            throw new CourseFullException("full");
        }
    }

    /**
     * Check whether a matching enrollment exists.
     *
     * @param student the student to check
     * @param course  the course to check
     * @return true if a matching enrollment exists, otherwise false
     * @requires student != null and course != null
     * @modifies nothing
     * @effects search enrollment records without modifying them
     */
    private boolean isEnrolled(Student student, Course course) {
        boolean enrollmentExists = false;
        for (var record : enrollmentRecords) {
            if (record.matchesEnrollment(student, course)) {
                enrollmentExists = true;
            }
        }
        return enrollmentExists;
    }

    /**
     * Remove a student's enrollment from a course.
     *
     * @param student the student to remove
     * @param course  the course to withdraw from
     * @throws IllegalArgumentException if student or course is null
     * @throws NotEnrolledException     if no matching enrollment exists
     * @requires student and course are non-null for successful removal
     * @modifies enrollmentRecords
     * @effects remove matching enrollment records on success;
     * leave enrollment records unchanged on validation failure
     */
    public void remove(Student student, Course course) {
        validateParameters(course, student);
        if (!isEnrolled(student, course)) {
            throw new NotEnrolledException("no");
        }
        enrollmentRecords.removeIf(record -> record.matchesEnrollment(student, course));
    }
}
