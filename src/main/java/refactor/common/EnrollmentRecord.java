package refactor.common;

import java.time.LocalDateTime;
import refactor.common.exception.InvalidEntityException;

/**
 * Immutable enrollment record ADT.
 * Rep invariant: student, course and enrollmentTime
 * are all non-null.
 *
 * @author Vax4Cat
 */
public class EnrollmentRecord {

    private final Student student;
    private final Course course;
    private final LocalDateTime enrollmentTime;

    /**
     * Create an enrollment record.
     *
     * @param student the student
     * @param course  the course
     * @throws InvalidEntityException if student or course is null
     * @requires student != null and course != null
     * @modifies this
     * @effects create an enrollment record with current time
     */
    public EnrollmentRecord(Student student, Course course) {
        if (student == null || course == null) {
            throw new InvalidEntityException("Student and course cannot be null.");
        }

        this.student = student;
        this.course = course;
        this.enrollmentTime = LocalDateTime.now();

        checkRep();
    }

    /**
     * Create an enrollment record with a specified time.
     *
     * @param student        the student
     * @param course         the course
     * @param enrollmentTime the enrollment timestamp
     * @throws InvalidEntityException if any parameter is null
     * @requires student, course and enrollmentTime are non-null
     * @modifies this
     * @effects initialize a new enrollment record
     */
    public EnrollmentRecord(Student student, Course course,
                            LocalDateTime enrollmentTime) {
        if (student == null || course == null || enrollmentTime == null) {
            throw new InvalidEntityException(
                    "Student, course and enrollment time cannot be null.");
        }

        this.student = student;
        this.course = course;
        this.enrollmentTime = enrollmentTime;

        checkRep();
    }

    /**
     * Check the representation invariant.
     */
    private void checkRep() {
        assert student != null;
        assert course != null;
        assert enrollmentTime != null;
    }

    /**
     * Get the student.
     *
     * @return student
     * @requires true
     * @modifies nothing
     * @effects no side effects
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Get the course.
     *
     * @return course
     * @requires true
     * @modifies nothing
     * @effects no side effects
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Get enrollment time.
     *
     * @return enrollment timestamp
     * @requires true
     * @modifies nothing
     * @effects no side effects
     */
    public LocalDateTime getEnrollmentTime() {
        return enrollmentTime;
    }

    /**
     * Get the student.
     *
     * @return student
     * @requires true
     * @modifies nothing
     * @effects no side effects
     */
    public Student student() {
        return student;
    }

    /**
     * Get the course.
     *
     * @return course
     * @requires true
     * @modifies nothing
     * @effects no side effects
     */
    public Course course() {
        return course;
    }

    /**
     * Check whether this record matches the given student and course.
     *
     * @param student the student to match
     * @param course  the course to match
     * @return true if both match, otherwise false
     * @requires true
     * @modifies nothing
     * @effects compare the student and course using their equals methods
     */
    public boolean matchesEnrollment(Student student, Course course) {
        return student().equals(student) && course().equals(course);
    }
}
