/*DROP DATABASE IF EXISTS wolf_wash;*/
/*CREATE DATABASE wolf_wash;*/
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

USE wolf_wash;

UPDATE time_slots SET slot_date = '2026-11-23' WHERE slot_id > 0;

INSERT INTO students (student_id, full_name, password_hash, balance)
VALUES ('620173162', 'Test Student', 'password123', 50.00);

INSERT INTO bookings (student_id, slot_id, status, payment_status)
VALUES ('620173162', 1, 'ACTIVE', 'PAID');

USE wolf_wash;
SELECT * FROM bookings WHERE student_id = '620173162';
SELECT slot_id, status FROM time_slots WHERE slot_id IN (1, 3);

insert into bookings(student_id,slot_id,status,payment_status)
values (620173163, 2 ,'ACTIVE','UNPAID');

INSERT INTO students (student_id, full_name, password_hash, balance)
VALUES ('620173163', 'Lawrey Layne', 'password@', 500.00);

USE wolf_wash;
INSERT INTO time_slots (machine_id, dryer_id, slot_date, start_time, end_time, status)
VALUES (3, 3, '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE');

USE wolf_wash;

INSERT INTO time_slots (machine_id, dryer_id, slot_date, start_time, end_time, status) VALUES
-- 12:00 PM - 01:30 PM
(1,  1,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(2,  2,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(4,  4,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(5,  5,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(6,  6,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(7,  7,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(8,  8,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(9,  9,  '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),
(10, 10, '2026-11-23', '12:00:00', '13:30:00', 'AVAILABLE'),

-- 01:30 PM - 03:00 PM
(1,  1,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(2,  2,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(3,  3,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(4,  4,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(5,  5,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(6,  6,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(7,  7,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(8,  8,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(9,  9,  '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),
(10, 10, '2026-11-23', '13:30:00', '15:00:00', 'AVAILABLE'),

-- 03:00 PM - 04:30 PM
(1,  1,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(2,  2,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(3,  3,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(4,  4,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(5,  5,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(6,  6,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(7,  7,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(8,  8,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(9,  9,  '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),
(10, 10, '2026-11-23', '15:00:00', '16:30:00', 'AVAILABLE'),

-- 04:30 PM - 06:00 PM
(1,  1,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(2,  2,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(3,  3,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(4,  4,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(5,  5,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(6,  6,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(7,  7,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(8,  8,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(9,  9,  '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),
(10, 10, '2026-11-23', '16:30:00', '18:00:00', 'AVAILABLE'),

-- 06:00 PM - 07:30 PM
(1,  1,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(2,  2,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(3,  3,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(4,  4,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(5,  5,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(6,  6,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(7,  7,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(8,  8,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(9,  9,  '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),
(10, 10, '2026-11-23', '18:00:00', '19:30:00', 'AVAILABLE'),

-- 07:30 PM - 09:00 PM
(1,  1,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(2,  2,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(3,  3,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(4,  4,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(5,  5,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(6,  6,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(7,  7,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(8,  8,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(9,  9,  '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),
(10, 10, '2026-11-23', '19:30:00', '21:00:00', 'AVAILABLE'),

-- 09:00 PM - 10:30 PM (last full 90-min slot before 11:00 PM close)
(1,  1,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(2,  2,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(3,  3,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(4,  4,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(5,  5,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(6,  6,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(7,  7,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(8,  8,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(9,  9,  '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE'),
(10, 10, '2026-11-23', '21:00:00', '22:30:00', 'AVAILABLE');

USE wolf_wash;

-- 1. Admins Table
CREATE TABLE IF NOT EXISTS admins (
    admin_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'MAINTENANCE') DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE time_slots
    ADD COLUMN status_reason VARCHAR(255) NULL,
    ADD COLUMN updated_by VARCHAR(20) NULL,
    ADD CONSTRAINT fk_slots_updated_by
        FOREIGN KEY (updated_by) REFERENCES admins(admin_id);


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
(10, 'Washer 10', 'OUT_OF_SERVICE');


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
(10, 'Dryer 10', 'OUT_OF_SERVICE');
