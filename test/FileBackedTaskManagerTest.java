import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    @Override
    void setUp() {
        try {
            this.tempFile = Files.createTempFile("task_manager_test", ".csv").toFile();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create temp file for test", e);
        }
        super.setUp();
    }

    @AfterEach
    void deleteTemFile() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(this.tempFile.toPath());
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(tempFile);
    }

    @Test
    void correctLoadingAndUnloading() {
        Task task = new Task("Тесты", "Не хватает времени написать тесты");
        this.taskManager.addTask(task);
        Epic epic = new Epic("Завершить 7 спринт", "важное дело, чтобы начать восьмой");
        this.taskManager.addEpic(epic);
        Subtask subtask = new Subtask(epic.getId(), "доделать метод записи", "Весьма непростое занятие");
        this.taskManager.addSubtask(subtask);
        FileBackedTaskManager loadingFile = FileBackedTaskManager.loadFromFile(tempFile);
        assertNotNull(loadingFile, "Менеджер не должен быть null");
        int tasksNumber = loadingFile.getTasks().size() + loadingFile.getEpics().size()
                + loadingFile.getSubtasks().size();
        assertEquals(3, tasksNumber, "Количество загруженных задач не совпадает");
    }
}