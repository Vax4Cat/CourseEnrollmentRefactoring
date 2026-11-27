package refactor.baseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import refactor.common.Course;
import refactor.common.Student;
import refactor.common.exception.CourseFullException;
import refactor.common.exception.EnrollmentConflictException;
import refactor.common.exception.NotEnrolledException;

import static org.junit.jupiter.api.Assertions.*;

class EnrollSvcTest {

    private EnrollSvc service;
    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        service = new EnrollSvc();
        student = new Student("S001", "Alice");
        course = new Course("C001", "Software Construction", 3, 30);
    }

    // 测试正常选课
    @Test
    void testAddSuccess() {
        service.add(student, course);

        assertEquals(1, service.recs.size());
        assertEquals(student, service.recs.get(0).student());
        assertEquals(course, service.recs.get(0).course());
        assertNotNull(service.recs.get(0).getEnrollmentTime());
    }

    // 测试重复选课
    @Test
    void testDuplicateEnrollment() {
        service.add(student, course);

        assertThrows(EnrollmentConflictException.class,
                () -> service.add(student, course));

        assertEquals(1, service.recs.size());
    }

    // 测试选课时学生为空
    @Test
    void testAddNullStudent() {
        assertThrows(IllegalArgumentException.class,
                () -> service.add(null, course));

        assertTrue(service.recs.isEmpty());
    }

    // 测试选课时课程为空
    @Test
    void testAddNullCourse() {
        assertThrows(IllegalArgumentException.class,
                () -> service.add(student, null));

        assertTrue(service.recs.isEmpty());
    }

    // 测试课程容量上限
    @Test
    void testCourseFull() {
        for (int i = 0; i < 30; i++) {
            Student s = new Student("S" + i, "Student" + i);
            service.add(s, course);
        }

        assertEquals(30, service.recs.size());

        assertThrows(CourseFullException.class,
                () -> service.add(student, course));

        assertEquals(30, service.recs.size());
    }

    // 测试正常退课
    @Test
    void testRemoveSuccess() {
        service.add(student, course);
        service.remove(student, course);

        assertTrue(service.recs.isEmpty());
    }

    // 测试未选课时退课
    @Test
    void testRemoveNotEnrolled() {
        assertThrows(NotEnrolledException.class,
                () -> service.remove(student, course));

        assertTrue(service.recs.isEmpty());
    }

    // 测试退课时学生为空
    @Test
    void testRemoveNullStudent() {
        assertThrows(IllegalArgumentException.class,
                () -> service.remove(null, course));
    }

    // 测试退课时课程为空
    @Test
    void testRemoveNullCourse() {
        assertThrows(IllegalArgumentException.class,
                () -> service.remove(student, null));
    }

    // 测试退课后重新选课
    @Test
    void testAddAfterRemove() {
        service.add(student, course);
        service.remove(student, course);
        service.add(student, course);

        assertEquals(1, service.recs.size());
    }
}