import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();

        task1 = new Task("Task 1", "Desc 1");
        task1.setId(1);
        task2 = new Task("Task 2", "Desc 2");
        task2.setId(2);
        task3 = new Task("Task 3", "Desc 3");
        task3.setId(3);
    }

@Test
    void shouldAddTasksAndKeepOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null.");
        assertEquals(3, history.size(), "Неверное количество задач в истории.");

        assertEquals(task1, history.get(0), "Первым должен быть task1.");
        assertEquals(task2, history.get(1), "Вторым должен быть task2.");
        assertEquals(task3, history.get(2), "Третьим должен быть task3.");
    }

}