USE wolf_wash;

-- Feature 5: Management of Reported Issues
CREATE TABLE IF NOT EXISTS reported_issues (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(9) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('PENDING','IN_PROGRESS','RESOLVED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);

CREATE TABLE IF NOT EXISTS issue_updates (
    update_id INT AUTO_INCREMENT PRIMARY KEY,
    issue_id INT NOT NULL,
    admin_id VARCHAR(20) NOT NULL,
    old_status ENUM('PENDING','IN_PROGRESS','RESOLVED') NULL,
    new_status ENUM('PENDING','IN_PROGRESS','RESOLVED') NULL,
    note TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (issue_id) REFERENCES reported_issues(issue_id),
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id)
);

CREATE TABLE IF NOT EXISTS issue_notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    issue_id INT NOT NULL,
    student_id VARCHAR(9) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_status ENUM('LOGGED','SENT','FAILED') NOT NULL DEFAULT 'LOGGED',
    FOREIGN KEY (issue_id) REFERENCES reported_issues(issue_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);
