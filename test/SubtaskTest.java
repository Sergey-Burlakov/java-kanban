import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void testEquals_sameId_shouldBeEqual() {
        Subtask subtask1 = new Subtask(1, "Имя", "Описание");
        subtask1.setId(1);
        Subtask subtask2 = new Subtask(2, "Другое имя", "Другое описание");
        subtask2.setId(1);
        assertEquals(subtask1, subtask2);
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic_epicId() {
        Subtask subtask = new Subtask(1, "Имя", "Описание");
        int initialEpicId = subtask.getEpicId();
        subtask.setId(10);
        subtask.setEpicId(subtask.getId());
        assertEquals(initialEpicId, subtask.getEpicId(), "epicId должен остаться прежним");
    }
}