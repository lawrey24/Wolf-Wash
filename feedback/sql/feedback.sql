USE wolf_wash;

CREATE TABLE IF NOT EXISTS feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(9) NOT NULL,
    type ENUM('FEEDBACK', 'SUGGESTION') NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status ENUM('NEW', 'IN_REVIEW', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'NEW',
    admin_note VARCHAR(1000) NULL,
    updated_by VARCHAR(20) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_student FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_feedback_admin FOREIGN KEY (updated_by) REFERENCES admins(admin_id)
);
