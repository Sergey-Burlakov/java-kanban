import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    public void testEquals_sameId_shouldBeEqual() {
        Task task1 = new Task("Имя", "Описание");
        task1.setId(1);
        Task task2 = new Task("Другое имя", "Другое описание");
        task2.setId(1);
        assertEquals(task1, task2);
    }

}