package refactor.baseline;

import refactor.common.*;
import refactor.common.exception.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class EnrollSvc {
    List<EnrollmentRecord> enrollmentRecords = new ArrayList<>();

    public void add(Student student, Course course){
        // duplicated null check
        if(student == null){
            throw new IllegalArgumentException("null");
        }
        if(course == null){
            throw new IllegalArgumentException("null");
        }

        // check exists
        boolean enrollmentExists = false;
        for(var record : enrollmentRecords){
            if(record.student().equals(student) && record.course().equals(course)){
                enrollmentExists = true;
            }
        }
        if(enrollmentExists){
            throw new EnrollmentConflictException("conflict");
        }

        // count
        int enrollmentCount = 0;
        for(var record : enrollmentRecords){
            if(record.course().equals(course)) enrollmentCount++;
        }
        if(enrollmentCount >= 30){ // magic number, default capacity
            throw new CourseFullException("full");
        }

        enrollmentRecords.add(new EnrollmentRecord(student,course, LocalDateTime.now()));
        System.out.println("ok"); // mixed IO with service logic
    }

    public void remove(Student student, Course course){
        if(student == null){
            throw new IllegalArgumentException("null");
        }
        if(course == null){
            throw new IllegalArgumentException("null");
        }
        boolean enrollmentExists = false;
        for(var record : enrollmentRecords){
            if(record.student().equals(student) && record.course().equals(course)){
                enrollmentExists = true;
            }
        }
        if(!enrollmentExists){
            throw new NotEnrolledException("no");
        }
        enrollmentRecords.removeIf(r -> r.student().equals(student) && r.course().equals(course));
    }
}
