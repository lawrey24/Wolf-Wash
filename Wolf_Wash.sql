DROP DATABASE IF EXISTS wolf_wash;
CREATE DATABASE wolf_wash;
USE wolf_wash;

-- 1. Students Table
CREATE TABLE students (
    student_id VARCHAR(9) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.00
);

-- 2. Time Slots Table (10 Washers, 10 Dryers, 90-min cycles)
CREATE TABLE time_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    machine_id INT NOT NULL,
    dryer_id INT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('AVAILABLE', 'BOOKED', 'OUT_OF_SERVICE') DEFAULT 'AVAILABLE',
    UNIQUE (machine_id, dryer_id, slot_date, start_time)
);

-- 3. Bookings Table
CREATE TABLE bookings (
    student_id VARCHAR(9) NOT NULL,
    slot_id INT NOT NULL,
    status ENUM('ACTIVE', 'CANCELLED', 'RESCHEDULED') DEFAULT 'ACTIVE',
    payment_status ENUM('UNPAID', 'PAID', 'REFUNDED') DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, slot_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (slot_id) REFERENCES time_slots(slot_id)
);

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(9) NOT NULL,
    slot_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CARD','CASH') NOT NULL,
    payment_status ENUM('PAID','UNPAID') DEFAULT 'UNPAID',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id, slot_id) REFERENCES bookings(student_id, slot_id)
);

-- ==========================================
-- SAMPLE DATA
-- ==========================================

INSERT INTO time_slots (machine_id, dryer_id, slot_date, start_time, end_time, status) VALUES
-- 09:00 AM - 10:30 AM (35m wash + 45m dry + 10m buffer)
(1,  1,  '2026-11-20', '09:00:00', '10:30:00', 'BOOKED'),
(2,  2,  '2026-11-20', '09:00:00', '10:30:00', 'BOOKED'),
(3,  3,  '2026-11-20', '09:00:00', '10:30:00', 'AVAILABLE'),
(4,  4,  '2026-11-20', '09:00:00', '10:30:00', 'AVAILABLE'),
(5,  5,  '2026-11-20', '09:00:00', '10:30:00', 'AVAILABLE'),
(6,  6,  '2026-11-20', '09:00:00', '10:30:00', 'AVAILABLE'),
(7,  7,  '2026-11-20', '09:00:00', '10:30:00', 'AVAILABLE'),
(8,  8,  '2026-11-20', '09:00:00', '10:30:00', 'AVAILABLE'),
(9,  9,  '2026-11-20', '09:00:00', '10:30:00', 'AVAILABLE'),
(10, 10, '2026-11-20', '09:00:00', '10:30:00', 'OUT_OF_SERVICE'),

-- 10:30 AM - 12:00 PM
(1,  1,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(2,  2,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(3,  3,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(4,  4,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(5,  5,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(6,  6,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(7,  7,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(8,  8,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(9,  9,  '2026-11-20', '10:30:00', '12:00:00', 'AVAILABLE'),
(10, 10, '2026-11-20', '10:30:00', '12:00:00', 'OUT_OF_SERVICE');
