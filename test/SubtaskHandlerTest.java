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

public class SubtaskHandlerTest {
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;

    public SubtaskHandlerTest() throws IOException {
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
    public void testAddSubtask() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic("Epic name", "epic description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask(1, "Test 2", "Testing task 2", LocalDateTime.now(), Duration.ofMinutes(5));
        // конвертируем её в JSON
        String subtaskJson = gson.toJson(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Subtask> tasksFromManager = manager.getSubtasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        // создаём задачу
        Epic epic = new Epic("Epic name", "epic description");
        manager.addEpic(epic);
        Subtask subtask = new Subtask(1, "Test 2", "Testing task 2", LocalDateTime.now(), Duration.ofMinutes(5));
        // добавляем ее в менеджер
        manager.addSubtask(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .GET()
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(200, response.statusCode());
        Subtask taskFromServer = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getName(), taskFromServer.getName(), "Некорректное имя задачи");
        assertEquals(subtask.getDescription(), taskFromServer.getDescription(), "Некорректное описание задачи");
        assertEquals(subtask.getStatus(), taskFromServer.getStatus(), "Некорректный статус задачи");
        assertEquals(subtask.getId(), taskFromServer.getId(), "Некорректный id задачи");
        assertEquals(subtask.getDuration(), taskFromServer.getDuration(), "Некорректная продолжительность " +
                "задачи");
        assertEquals(subtask.getStartTime().get().truncatedTo(ChronoUnit.MINUTES), taskFromServer.getStartTime().get()
                .truncatedTo(ChronoUnit.MINUTES), "Некорректное время начала задачи");
        assertEquals(subtask.getEndTime().get().truncatedTo(ChronoUnit.MINUTES), taskFromServer.getEndTime().get()
                .truncatedTo(ChronoUnit.MINUTES), "Некорректное время окончания задачи");
    }

    @Test
    public void testGetTaskByI404() throws IOException, InterruptedException {
        // создаём задачу
        Subtask subtask = new Subtask(1, "Test 2", "Testing task 2", LocalDateTime.now(), Duration.ofMinutes(5));
        // добавляем ее в менеджер
        manager.addSubtask(subtask);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/2");
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

