USE wolf_wash;
CREATE TABLE IF NOT EXISTS terminal_notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(9) NOT NULL,
    booking_id INT NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
    attempted_at DATETIME(6),
    sent_at DATETIME(6),
    UNIQUE (booking_id,event_type),
    INDEX (student_id,status,notification_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);
