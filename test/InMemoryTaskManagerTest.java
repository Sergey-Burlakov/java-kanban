import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    @Test
    void testGetDefault_historyManagerNotNull() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(((InMemoryTaskManager) taskManager).getHistoryManager(), "HistoryManager не должен быть null");
    }


}