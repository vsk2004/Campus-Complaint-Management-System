package com.ccms.repository;

import com.ccms.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, String> {

    List<Complaint> findByStudentId(String studentId);

    @Query("""
            SELECT c FROM Complaint c
            WHERE c.studentId = :studentId
            ORDER BY
                CASE LOWER(c.status)
                    WHEN 'resolved' THEN 1
                    ELSE 0
                END,
                CASE LOWER(c.urgency)
                    WHEN 'critical' THEN 0
                    WHEN 'high' THEN 1
                    WHEN 'medium' THEN 2
                    WHEN 'low' THEN 3
                    ELSE 4
                END,
                c.createdAt DESC
            """)
    List<Complaint> findByStudentIdOrderByPriority(@Param("studentId") String studentId);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT c FROM Complaint c
            WHERE c.department = :department
            ORDER BY
                CASE LOWER(c.status)
                    WHEN 'resolved' THEN 1
                    ELSE 0
                END,
                CASE LOWER(c.urgency)
                    WHEN 'critical' THEN 0
                    WHEN 'high' THEN 1
                    WHEN 'medium' THEN 2
                    WHEN 'low' THEN 3
                    ELSE 4
                END,
                c.createdAt DESC
            """)
    List<Complaint> findByDepartmentOrderByPriority(@Param("department") String department);
}
