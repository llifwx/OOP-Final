package model.academic;

import enums.RegistrationStatus;
import model.users.Manager;
import model.users.Student;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class CourseRegistration implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 0;

    private int id;
    private Student student;
    private Course course;
    private RegistrationStatus status;
    private Manager reviewedBy;
    private String rejectionReason;
    private Date createdDate;
    private Date reviewedDate;

    public CourseRegistration(Student student, Course course) {
        this.id = ++idCounter;
        this.student = student;
        this.course = course;
        this.status = RegistrationStatus.PENDING;
        this.createdDate = new Date();
    }

    public static void synchronizeIdCounter(int maxId) {
        if (maxId > idCounter) {
            idCounter = maxId;
        }
    }

    public void ensureId() {
        if (id <= 0) {
            id = ++idCounter;
        }
    }

    public int getId() {return id;}

    public Student getStudent() {return student;}

    public Course getCourse() {return course;}

    public RegistrationStatus getStatus() {return status;}

    public Manager getReviewedBy() {return reviewedBy;}

    public String getRejectionReason() {return rejectionReason;}

    public Date getCreatedDate() {return createdDate == null ? null : new Date(createdDate.getTime());}

    public Date getReviewedDate() {return reviewedDate == null ? null : new Date(reviewedDate.getTime());}

    public void approve(Manager manager) {
        this.status = RegistrationStatus.APPROVED;
        this.reviewedBy = manager;
        this.rejectionReason = null;
        this.reviewedDate = new Date();
    }

    public void reject(Manager manager, String reason) {
        this.status = RegistrationStatus.REJECTED;
        this.reviewedBy = manager;
        this.rejectionReason = reason;
        this.reviewedDate = new Date();
    }

    @Override
    public String toString() {
        return "CourseRegistration{" + "id=" + id
                + ", student=" + (student != null ? student.getFullName() : "N/A")
                + ", course=" + (course != null ? course.getCourseCode() : "N/A")
                + ", status=" + status
                + ", createdDate=" + createdDate
                + ", reviewedDate=" + reviewedDate
                + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CourseRegistration registration)) return false;
        return id == registration.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
