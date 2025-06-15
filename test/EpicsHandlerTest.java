import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EpicsHandlerTest {
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;

    public EpicsHandlerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        this.manager = new InMemoryTaskManager();
        this.taskServer = new HttpTaskServer(manager);
        this.gson = taskServer.getGson();
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic("Test 2", "Testing task 2");
        // конвертируем её в JSON
        String taskJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Epic> tasksFromManager = manager.getEpics();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing task 2");
        manager.addEpic(epic);
        Subtask subtask = new Subtask(1, "Test 2", "Testing task 2", LocalDateTime.now(),
                Duration.ofMinutes(5));
        manager.addSubtask(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());
        Epic taskFromServer = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getName(), taskFromServer.getName(), "Некорректное имя задачи");
        assertEquals(epic.getDescription(), taskFromServer.getDescription(), "Некорректное описание задачи");
        assertEquals(epic.getStatus(), taskFromServer.getStatus(), "Некорректный статус задачи");
        assertEquals(epic.getId(), taskFromServer.getId(), "Некорректный id задачи");
        assertEquals(epic.getDuration(), taskFromServer.getDuration(), "Некорректная продолжительность " +
                "задачи");
        assertEquals(epic.getStartTime().get().truncatedTo(ChronoUnit.MINUTES), taskFromServer.getStartTime().get()
                .truncatedTo(ChronoUnit.MINUTES), "Некорректное время начала задачи");
        assertEquals(epic.getEndTime().get().truncatedTo(ChronoUnit.MINUTES), taskFromServer.getEndTime().get()
                .truncatedTo(ChronoUnit.MINUTES), "Некорректное время окончания задачи");
    }

    @Test
    public void testGetTaskByI404() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing task 2");
        manager.addEpic(epic);
        Subtask subtask = new Subtask(1, "Test 2", "Testing task 2", LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addTask(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/2");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

}

