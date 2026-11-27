package refactor.baseline;

import refactor.common.*;
import refactor.common.exception.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class EnrollSvc {
    private static final int DEFAULT_CAPACITY = 30;
    List<EnrollmentRecord> enrollmentRecords = new ArrayList<>();

    public void add(Student student, Course course){
        // duplicated null check
        validateParameters(student, course);

        // check exists
        boolean enrollmentExists = isEnrolled(student, course);
        if(enrollmentExists){
            throw new EnrollmentConflictException("conflict");
        }

        // count
        validateCapacity(course);

        enrollmentRecords.add(new EnrollmentRecord(student,course, LocalDateTime.now()));
        System.out.println("ok"); // mixed IO with service logic
    }

    private void validateCapacity(Course course) {
        int enrollmentCount = 0;
        for(var record : enrollmentRecords){
            if(record.course().equals(course)) enrollmentCount++;
        }
        if(enrollmentCount >= DEFAULT_CAPACITY){ // magic number, default capacity
            throw new CourseFullException("full");
        }
    }

    private boolean isEnrolled(Student student, Course course) {
        boolean enrollmentExists = false;
        for(var record : enrollmentRecords){
            if(record.student().equals(student) && record.course().equals(course)){
                enrollmentExists = true;
            }
        }
        return enrollmentExists;
    }

    private static void validateParameters(Student student, Course course) {
        if(student == null){
            throw new IllegalArgumentException("null");
        }
        if(course == null){
            throw new IllegalArgumentException("null");
        }
    }

    public void remove(Student student, Course course){
        validateParameters(student, course);
        boolean enrollmentExists = isEnrolled(student, course);
        if(!enrollmentExists){
            throw new NotEnrolledException("no");
        }
        enrollmentRecords.removeIf(r -> r.student().equals(student) && r.course().equals(course));
    }
}
