# Wolf Wash

Java/Maven project with layered packages:

- `view`: console menus and presentation
- `controller`: input coordination and authenticated access checks
- `service`: validation and business logic
- `dao`: database queries and persistence
- `model`: domain objects

## Feature 5 — Management of Reported Issues

Run the SQL migration `feature5_schema.sql` against the `wolf_wash` database. It creates issue, progress/status-history, and notification-log tables. The application's main menu includes an administrator issue-management entry.

Admin credentials are checked against the existing `admins` table. The migration assumes that table and the `students` table have already been created by `Wolf_Wash.sql`.

Admin menu supports viewing all issues, status filters, updating statuses (Pending, In Progress, Resolved), progress notes, history, and the notification log. Notification entries are recorded in the database; this implementation does not connect to an email provider, so the log is not proof that an email was sent.

## Database connection

Set environment variables before launching:

- `DB_URL` (optional; defaults to `jdbc:mysql://localhost:3306/wolf_wash`)
- `DB_USERNAME` (optional; defaults to `root`)
- `DB_PASSWORD` (required)

Build with Maven: `mvn package`.
