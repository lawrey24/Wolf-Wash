-- Initialize once on a fresh installation.
CREATE DATABASE IF NOT EXISTS wolf_wash;
USE wolf_wash;

CREATE TABLE students (
    student_id VARCHAR(9) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS admins (
    admin_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'MAINTENANCE') DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================
-- FEEDBACK & SUGGESTIONS
-- =====================================

CREATE TABLE IF NOT EXISTS feedback (

    feedback_id INT AUTO_INCREMENT PRIMARY KEY,

    student_id VARCHAR(9) NOT NULL,

    type ENUM('FEEDBACK', 'SUGGESTION') NOT NULL,

    title VARCHAR(150) NULL,

    message VARCHAR(1000) NOT NULL,

    status ENUM('NEW', 'IN_REVIEW', 'RESOLVED', 'CLOSED')
        NOT NULL DEFAULT 'NEW',

    admin_note VARCHAR(1000) NULL,

    updated_by VARCHAR(20) NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_feedback_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    CONSTRAINT fk_feedback_admin
        FOREIGN KEY (updated_by)
        REFERENCES admins(admin_id)

);




CREATE TABLE IF NOT EXISTS admin_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id VARCHAR(20) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id)
);

-- Sample Admin Account for testing
INSERT INTO admins (admin_id, full_name, password_hash, role)
VALUES ('ADM001', 'Hall Administrator', 'admin123', 'ADMIN');

CREATE TABLE IF NOT EXISTS machines (
    machine_id INT PRIMARY KEY,
    machine_label VARCHAR(50) NOT NULL,
    status ENUM('AVAILABLE', 'OUT_OF_SERVICE') DEFAULT 'AVAILABLE',
    out_of_service_reason VARCHAR(255) NULL,
    updated_by VARCHAR(20) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES admins(admin_id)
);

INSERT IGNORE INTO machines (machine_id, machine_label, status) VALUES
(1, 'Washer 1', 'AVAILABLE'),
(2, 'Washer 2', 'AVAILABLE'),
(3, 'Washer 3', 'AVAILABLE'),
(4, 'Washer 4', 'AVAILABLE'),
(5, 'Washer 5', 'AVAILABLE'),
(6, 'Washer 6', 'AVAILABLE'),
(7, 'Washer 7', 'AVAILABLE'),
(8, 'Washer 8', 'AVAILABLE'),
(9, 'Washer 9', 'AVAILABLE'),
(10, 'Washer 10', 'AVAILABLE');


USE wolf_wash;

CREATE TABLE IF NOT EXISTS dryers (
    dryer_id INT PRIMARY KEY,
    dryer_label VARCHAR(50) NOT NULL,
    status ENUM('AVAILABLE', 'OUT_OF_SERVICE') DEFAULT 'AVAILABLE',
    out_of_service_reason VARCHAR(255) NULL,
    updated_by VARCHAR(20) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (updated_by) REFERENCES admins(admin_id)
);

INSERT IGNORE INTO dryers (dryer_id, dryer_label, status) VALUES
(1,  'Dryer 1',  'AVAILABLE'),
(2,  'Dryer 2',  'AVAILABLE'),
(3,  'Dryer 3',  'AVAILABLE'),
(4,  'Dryer 4',  'AVAILABLE'),
(5,  'Dryer 5',  'AVAILABLE'),
(6,  'Dryer 6',  'AVAILABLE'),
(7,  'Dryer 7',  'AVAILABLE'),
(8,  'Dryer 8',  'AVAILABLE'),
(9,  'Dryer 9',  'AVAILABLE'),
(10, 'Dryer 10', 'AVAILABLE');

CREATE TABLE time_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    machine_id INT NOT NULL,
    dryer_id INT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('AVAILABLE', 'BOOKED', 'OUT_OF_SERVICE') DEFAULT 'AVAILABLE',
    status_reason VARCHAR(255) NULL,
    updated_by VARCHAR(20) NULL,
    FOREIGN KEY (updated_by) REFERENCES admins(admin_id),
    UNIQUE (machine_id, dryer_id, slot_date, start_time)
);

CREATE TABLE bookings(
 booking_id INT AUTO_INCREMENT PRIMARY KEY, student_id VARCHAR(9) NOT NULL, slot_id INT NOT NULL,
 status ENUM('ACTIVE','CANCELLED','RESCHEDULED') DEFAULT 'ACTIVE',
 payment_status ENUM('UNPAID','PAID','REFUNDED','REFUND_PENDING','CREDITED') DEFAULT 'UNPAID',
 refund_due DATETIME NULL, fee DECIMAL(10,2) NOT NULL DEFAULT 400.00,
 active_student VARCHAR(9) GENERATED ALWAYS AS (IF(status='ACTIVE',student_id,NULL)) STORED UNIQUE,
 active_slot INT GENERATED ALWAYS AS (IF(status='ACTIVE',slot_id,NULL)) STORED UNIQUE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(student_id) REFERENCES students(student_id),FOREIGN KEY(slot_id) REFERENCES time_slots(slot_id));
 CREATE TABLE saved_cards(card_id INT AUTO_INCREMENT PRIMARY KEY, student_id VARCHAR(9) NOT NULL,
 token VARCHAR(40) NOT NULL,last_four CHAR(4) NOT NULL,expiry CHAR(7) NOT NULL,
 UNIQUE(student_id,token,expiry),FOREIGN KEY(student_id) REFERENCES students(student_id));
 CREATE TABLE payments(payment_id INT AUTO_INCREMENT PRIMARY KEY, booking_id INT NOT NULL UNIQUE,
 amount DECIMAL(10,2) NOT NULL, method VARCHAR(20) NOT NULL, paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(booking_id) REFERENCES bookings(booking_id));

INSERT INTO students (student_id, full_name, password_hash, balance)
VALUES ('620173162', 'Test Student', 'password123', 0.00);

SET @slot_date = CURRENT_DATE + INTERVAL 1 DAY;
INSERT INTO time_slots (machine_id, dryer_id, slot_date, start_time, end_time) VALUES
    (1, 1, @slot_date, '09:00:00', '10:30:00'),
    (2, 2, @slot_date, '09:00:00', '10:30:00'),
    (3, 3, @slot_date, '09:00:00', '10:30:00'),
    (4, 4, @slot_date, '09:00:00', '10:30:00'),
    (5, 5, @slot_date, '09:00:00', '10:30:00'),
    (6, 6, @slot_date, '09:00:00', '10:30:00'),
    (7, 7, @slot_date, '09:00:00', '10:30:00'),
    (8, 8, @slot_date, '09:00:00', '10:30:00'),
    (9, 9, @slot_date, '09:00:00', '10:30:00'),
    (10, 10, @slot_date, '09:00:00', '10:30:00'),
    (1, 1, @slot_date, '10:30:00', '12:00:00'),
    (2, 2, @slot_date, '10:30:00', '12:00:00'),
    (3, 3, @slot_date, '10:30:00', '12:00:00'),
    (4, 4, @slot_date, '10:30:00', '12:00:00'),
    (5, 5, @slot_date, '10:30:00', '12:00:00'),
    (6, 6, @slot_date, '10:30:00', '12:00:00'),
    (7, 7, @slot_date, '10:30:00', '12:00:00'),
    (8, 8, @slot_date, '10:30:00', '12:00:00'),
    (9, 9, @slot_date, '10:30:00', '12:00:00'),
    (10, 10, @slot_date, '10:30:00', '12:00:00'),
    (3, 3, @slot_date, '12:00:00', '13:30:00'),
    (1, 1, @slot_date, '12:00:00', '13:30:00'),
    (2, 2, @slot_date, '12:00:00', '13:30:00'),
    (4, 4, @slot_date, '12:00:00', '13:30:00'),
    (5, 5, @slot_date, '12:00:00', '13:30:00'),
    (6, 6, @slot_date, '12:00:00', '13:30:00'),
    (7, 7, @slot_date, '12:00:00', '13:30:00'),
    (8, 8, @slot_date, '12:00:00', '13:30:00'),
    (9, 9, @slot_date, '12:00:00', '13:30:00'),
    (10, 10, @slot_date, '12:00:00', '13:30:00'),
    (1, 1, @slot_date, '13:30:00', '15:00:00'),
    (2, 2, @slot_date, '13:30:00', '15:00:00'),
    (3, 3, @slot_date, '13:30:00', '15:00:00'),
    (4, 4, @slot_date, '13:30:00', '15:00:00'),
    (5, 5, @slot_date, '13:30:00', '15:00:00'),
    (6, 6, @slot_date, '13:30:00', '15:00:00'),
    (7, 7, @slot_date, '13:30:00', '15:00:00'),
    (8, 8, @slot_date, '13:30:00', '15:00:00'),
    (9, 9, @slot_date, '13:30:00', '15:00:00'),
    (10, 10, @slot_date, '13:30:00', '15:00:00'),
    (1, 1, @slot_date, '15:00:00', '16:30:00'),
    (2, 2, @slot_date, '15:00:00', '16:30:00'),
    (3, 3, @slot_date, '15:00:00', '16:30:00'),
    (4, 4, @slot_date, '15:00:00', '16:30:00'),
    (5, 5, @slot_date, '15:00:00', '16:30:00'),
    (6, 6, @slot_date, '15:00:00', '16:30:00'),
    (7, 7, @slot_date, '15:00:00', '16:30:00'),
    (8, 8, @slot_date, '15:00:00', '16:30:00'),
    (9, 9, @slot_date, '15:00:00', '16:30:00'),
    (10, 10, @slot_date, '15:00:00', '16:30:00'),
    (1, 1, @slot_date, '16:30:00', '18:00:00'),
    (2, 2, @slot_date, '16:30:00', '18:00:00'),
    (3, 3, @slot_date, '16:30:00', '18:00:00'),
    (4, 4, @slot_date, '16:30:00', '18:00:00'),
    (5, 5, @slot_date, '16:30:00', '18:00:00'),
    (6, 6, @slot_date, '16:30:00', '18:00:00'),
    (7, 7, @slot_date, '16:30:00', '18:00:00'),
    (8, 8, @slot_date, '16:30:00', '18:00:00'),
    (9, 9, @slot_date, '16:30:00', '18:00:00'),
    (10, 10, @slot_date, '16:30:00', '18:00:00'),
    (1, 1, @slot_date, '18:00:00', '19:30:00'),
    (2, 2, @slot_date, '18:00:00', '19:30:00'),
    (3, 3, @slot_date, '18:00:00', '19:30:00'),
    (4, 4, @slot_date, '18:00:00', '19:30:00'),
    (5, 5, @slot_date, '18:00:00', '19:30:00'),
    (6, 6, @slot_date, '18:00:00', '19:30:00'),
    (7, 7, @slot_date, '18:00:00', '19:30:00'),
    (8, 8, @slot_date, '18:00:00', '19:30:00'),
    (9, 9, @slot_date, '18:00:00', '19:30:00'),
    (10, 10, @slot_date, '18:00:00', '19:30:00'),
    (1, 1, @slot_date, '19:30:00', '21:00:00'),
    (2, 2, @slot_date, '19:30:00', '21:00:00'),
    (3, 3, @slot_date, '19:30:00', '21:00:00'),
    (4, 4, @slot_date, '19:30:00', '21:00:00'),
    (5, 5, @slot_date, '19:30:00', '21:00:00'),
    (6, 6, @slot_date, '19:30:00', '21:00:00'),
    (7, 7, @slot_date, '19:30:00', '21:00:00'),
    (8, 8, @slot_date, '19:30:00', '21:00:00'),
    (9, 9, @slot_date, '19:30:00', '21:00:00'),
    (10, 10, @slot_date, '19:30:00', '21:00:00'),
    (1, 1, @slot_date, '21:00:00', '22:30:00'),
    (2, 2, @slot_date, '21:00:00', '22:30:00'),
    (3, 3, @slot_date, '21:00:00', '22:30:00'),
    (4, 4, @slot_date, '21:00:00', '22:30:00'),
    (5, 5, @slot_date, '21:00:00', '22:30:00'),
    (6, 6, @slot_date, '21:00:00', '22:30:00'),
    (7, 7, @slot_date, '21:00:00', '22:30:00'),
    (8, 8, @slot_date, '21:00:00', '22:30:00'),
    (9, 9, @slot_date, '21:00:00', '22:30:00'),
    (10, 10, @slot_date, '21:00:00', '22:30:00');
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
