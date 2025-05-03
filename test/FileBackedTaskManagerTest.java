import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager fileManager;
    private File tempFile;
    @BeforeEach
    void setUp() throws IOException {
        this.tempFile = Files.createTempFile("task_manager_test", ".csv").toFile();
        this.fileManager = new FileBackedTaskManager(tempFile);
    }
    @AfterEach
    void deleteTemFile() throws IOException {
        Files.deleteIfExists(this.tempFile.toPath());
    }
    @Test
    void correctLoadingAndUnloading() {
        Task task = new Task("Тесты", "Не хватает времени написать тесты(");
        fileManager.addTask(task);
        Epic epic = new Epic("Завершить 7 спринт","важное дело, чтобы начать восьмой");
        fileManager.addEpic(epic);
        Subtask subtask = new Subtask(epic.getId(),"доделать метод записи","Весьма непростое занятие");
        fileManager.addSubtask(subtask);
        FileBackedTaskManager loadingFile = FileBackedTaskManager.loadFromFile(tempFile);
        assertNotNull(loadingFile, "Менеджер не должен быть null");
        int tasksNumber = loadingFile.getTasks().size() + loadingFile.getEpics().size()
                + loadingFile.getSubtasks().size();
        assertEquals(3,tasksNumber,"Количество загруженных задач не совпадает");
    }
}