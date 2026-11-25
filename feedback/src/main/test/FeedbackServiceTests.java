package test;

import dao.FeedbackDao;
import model.Feedback;
import model.User;
import service.FeedbackService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight tests for FeedbackService.
 * These tests use an in-memory fake DAO, so they do not connect to MySQL.
 */
public class FeedbackServiceTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        FakeFeedbackDao dao = new FakeFeedbackDao();
        FeedbackService service = new FeedbackService(dao);
        User student = new User("620173162", "Test Student", false);
        User admin = new User("ADM001", "Hall Administrator", true);

        run("valid student login", () ->
            assertEquals("620173162", service.login("620173162", "password123").getId()));
        run("blank login ID is rejected", () ->
            assertNull(service.login("   ", "password123")));
        run("blank password is rejected", () ->
            assertNull(service.login("620173162", "")));

        run("valid feedback submission", () -> {
            service.submit(student, "FEEDBACK", "The washing area is clean.");
            assertEquals(1, dao.saved.size());
            assertEquals("FEEDBACK", dao.saved.get(0).category);
        });
        run("valid suggestion submission", () ->
            service.submit(student, "SUGGESTION", "Please add another washing machine."));
        run("blank message is rejected", () ->
            expectThrows(IllegalArgumentException.class,
                () -> service.submit(student, "FEEDBACK", "   ")));
        run("message shorter than 10 characters is rejected", () ->
            expectThrows(IllegalArgumentException.class,
                () -> service.submit(student, "FEEDBACK", "Too short")));
        run("message longer than 1000 characters is rejected", () ->
            expectThrows(IllegalArgumentException.class,
                () -> service.submit(student, "FEEDBACK", "x".repeat(1001))));
        run("invalid category is rejected", () ->
            expectThrows(IllegalArgumentException.class,
                () -> service.submit(student, "ISSUE", "This category is not allowed.")));
        run("admin cannot submit feedback", () ->
            expectThrows(IllegalArgumentException.class,
                () -> service.submit(admin, "FEEDBACK", "This should not be accepted.")));
        run("null user cannot submit feedback", () ->
            expectThrows(IllegalArgumentException.class,
                () -> service.submit(null, "FEEDBACK", "This should not be accepted.")));

        run("student can view own submissions", () ->
            assertEquals(1, service.myFeedback(student).size()));
        run("student cannot view all submissions", () ->
            expectThrows(SecurityException.class, () -> service.allFeedback(student)));
        run("admin can view all submissions", () ->
            assertEquals(1, service.allFeedback(admin).size()));

        run("student can delete own new submission", () ->
            assertTrue(service.delete(student, 10)));
        run("admin cannot delete a student submission", () ->
            assertFalse(service.delete(admin, 10)));
        run("null user cannot delete a submission", () ->
            assertFalse(service.delete(null, 10)));

        run("admin can update feedback status and note", () -> {
            service.update(admin, 10, "RESOLVED", "The issue has been addressed.");
            assertEquals("RESOLVED", dao.lastStatus);
            assertEquals("The issue has been addressed.", dao.lastNote);
            assertEquals("ADM001", dao.lastAdminId);
        });
        run("student cannot update feedback", () ->
            expectThrows(SecurityException.class,
                () -> service.update(student, 10, "RESOLVED", "Not allowed.")));
        run("invalid status is rejected", () ->
            expectThrows(IllegalArgumentException.class,
                () -> service.update(admin, 10, "NEW", "Invalid transition.")));
        run("missing feedback ID is reported", () -> {
            dao.updateResult = false;
            expectThrows(IllegalArgumentException.class,
                () -> service.update(admin, 999, "CLOSED", "Not found."));
            dao.updateResult = true;
        });

        System.out.println("\\nResults: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) throw new AssertionError("Some tests failed.");
    }

    private static void run(String name, TestCase test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable error) {
            failed++;
            System.out.println("[FAIL] " + name + " - " + error.getMessage());
        }
    }

    private static void assertTrue(boolean value) {
        if (!value) throw new AssertionError("Expected true.");
    }
    private static void assertFalse(boolean value) {
        if (value) throw new AssertionError("Expected false.");
    }
    private static void assertNull(Object value) {
        if (value != null) throw new AssertionError("Expected null, got " + value);
    }
    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual))
            throw new AssertionError("Expected " + expected + " but got " + actual);
    }
    private static void expectThrows(Class<? extends Throwable> expected, TestCase action) throws Exception {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) return;
            throw new AssertionError("Expected " + expected.getSimpleName()
                    + " but got " + actual.getClass().getSimpleName());
        }
        throw new AssertionError("Expected " + expected.getSimpleName() + " but nothing was thrown.");
    }

    @FunctionalInterface
    private interface TestCase { void run() throws Exception; }

    private static class FakeFeedbackDao extends FeedbackDao {
        private final List<SavedFeedback> saved = new ArrayList<>();
        private String lastStatus;
        private String lastNote;
        private String lastAdminId;
        private boolean updateResult = true;

        @Override
        public User login(String id, String password) throws SQLException {
            if ("620173162".equals(id) && "password123".equals(password))
                return new User(id, "Test Student", false);
            return null;
        }

        @Override
        public void save(User user, String category, String message) {
            saved.add(new SavedFeedback(user.getId(), category, message));
        }

        @Override
        public List<Feedback> findForUser(String userId) {
            return List.of(new Feedback(10, userId, "Test Student", "FEEDBACK",
                    "The washing area is clean.", "NEW", null, null));
        }

        @Override
        public List<Feedback> findAll() {
            return List.of(new Feedback(10, "620173162", "Test Student", "FEEDBACK",
                    "The washing area is clean.", "NEW", null, null));
        }

        @Override
        public boolean deleteOwn(int feedbackId, String userId) {
            return feedbackId == 10 && "620173162".equals(userId);
        }

        @Override
        public boolean updateStatus(int feedbackId, String status, String note, String adminId) {
            if (!updateResult || feedbackId != 10) return false;
            lastStatus = status;
            lastNote = note;
            lastAdminId = adminId;
            return true;
        }
    }

    private static class SavedFeedback {
        final String studentId, category, message;
        SavedFeedback(String studentId, String category, String message) {
            this.studentId = studentId;
            this.category = category;
            this.message = message;
        }
    }
}
