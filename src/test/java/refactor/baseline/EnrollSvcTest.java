package refactor.baseline;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import refactor.common.Course;
import refactor.common.Student;
import refactor.common.exception.CourseFullException;
import refactor.common.exception.EnrollmentConflictException;
import refactor.common.exception.NotEnrolledException;


class EnrollSvcTest {

    private static final int COURSE_CREDIT = 3;
    private static final int DEFAULT_CAPACITY = 30;
    private static final int ALMOST_FULL_CAPACITY = DEFAULT_CAPACITY - 1;
    private static final int TIMEOUT_SECONDS = 5;
    private EnrollSvc service;
    private Student student;
    private Course course;
    private final Object enrollmentLock = new Object();

    @BeforeEach
    void setUp() {
        service = new EnrollSvc();
        student = new Student("S001", "Alice");
        course = new Course("C001", "Software Construction", COURSE_CREDIT, DEFAULT_CAPACITY);
    }

    // 测试正常选课
    @Test
    void testAddSuccess() {
        service.add(student, course);
        assertEquals(1, service.enrollmentRecords.size());
        assertEquals(student, service.enrollmentRecords.getFirst().student());
        assertEquals(course, service.enrollmentRecords.getFirst().course());
        assertNotNull(service.enrollmentRecords.getFirst().getEnrollmentTime());
    }

    // 测试重复选课
    @Test
    void testDuplicateEnrollment() {
        service.add(student, course);
        assertThrows(EnrollmentConflictException.class,
                () -> service.add(student, course));
        assertEquals(1, service.enrollmentRecords.size());
    }

    // 测试选课时学生为空
    @Test
    void testAddNullStudent() {
        assertThrows(IllegalArgumentException.class,
                () -> service.add(null, course));
        assertTrue(service.enrollmentRecords.isEmpty());
    }

    // 测试选课时课程为空
    @Test
    void testAddNullCourse() {
        assertThrows(IllegalArgumentException.class,
                () -> service.add(student, null));
        assertTrue(service.enrollmentRecords.isEmpty());
    }

    // 测试课程容量上限
    @Test
    void testCourseFull() {
        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            Student s = new Student("S" + i, "Student" + i);
            service.add(s, course);
        }
        assertEquals(DEFAULT_CAPACITY, service.enrollmentRecords.size());
        assertThrows(CourseFullException.class,
                () -> service.add(student, course));
        assertEquals(DEFAULT_CAPACITY, service.enrollmentRecords.size());
    }

    // 测试正常退课
    @Test
    void testRemoveSuccess() {
        service.add(student, course);
        service.remove(student, course);
        assertTrue(service.enrollmentRecords.isEmpty());
    }

    // 测试未选课时退课
    @Test
    void testRemoveNotEnrolled() {
        assertThrows(NotEnrolledException.class,
                () -> service.remove(student, course));
        assertTrue(service.enrollmentRecords.isEmpty());
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
        assertEquals(1, service.enrollmentRecords.size());
    }

    @Test
    void testCapacityOne() {
        Course smallCourse = new Course(
                "C002", "Small Course", COURSE_CREDIT, 1);
        Student anotherStudent = new Student("S002", "Bob");
        service.add(student, smallCourse);
        assertDoesNotThrow(() ->
                service.add(anotherStudent, smallCourse));
        assertEquals(2, service.enrollmentRecords.size());
    }

    @Test
    void testEnrollmentAfterCapacityReleased() {
        service.add(student, course);
        prepareAlmostFullCourse();
        assertEquals(DEFAULT_CAPACITY, service.enrollmentRecords.size());
        Student newStudent = new Student("S999", "New Student");
        assertThrows(CourseFullException.class,
                () -> service.add(newStudent, course));
        service.remove(student, course);
        assertEquals(ALMOST_FULL_CAPACITY, service.enrollmentRecords.size());
        assertDoesNotThrow(() -> service.add(newStudent, course));
        assertEquals(DEFAULT_CAPACITY, service.enrollmentRecords.size());
    }

    @Test
    void testDuplicateEnrollmentWhenCourseIsFull() {
        service.add(student, course);
        prepareAlmostFullCourse();
        assertEquals(DEFAULT_CAPACITY, service.enrollmentRecords.size());
        EnrollmentConflictException exception =
                assertThrows(EnrollmentConflictException.class,
                        () -> service.add(student, course));
        assertEquals("conflict", exception.getMessage());
        assertEquals(DEFAULT_CAPACITY, service.enrollmentRecords.size());
    }

    @Test
    void testConcurrentEnrollmentBoundary() throws Exception {
        prepareAlmostFullCourse();
        Student first = new Student("S101", "First");
        Student second = new Student("S102", "Second");
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> result1 = executor.submit(() -> {
                start.await();
                return tryEnroll(first);
            });
            Future<Boolean> result2 = executor.submit(() -> {
                start.await();
                return tryEnroll(second);
            });
            start.countDown();
            boolean success1 = result1.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            boolean success2 = result2.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotEquals(success1, success2);
            assertEquals(DEFAULT_CAPACITY, service.enrollmentRecords.size());
        }
    }

    private void prepareAlmostFullCourse() {
        for (int i = 0; i < ALMOST_FULL_CAPACITY; i++) {
            Student other = new Student("T" + i, "Student" + i);
            service.add(other, course);
        }
    }

    private boolean tryEnroll(Student targetStudent) {
        synchronized (enrollmentLock) {
            try {
                service.add(targetStudent, course);
                return true;
            } catch (CourseFullException e) {
                return false;
            }
        }
    }
}