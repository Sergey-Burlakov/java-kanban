import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HistoryHandlerTest {
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;

    public HistoryHandlerTest() throws IOException {
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
    public void shouldReturnHistoryWhenHistoryIsNotEmpty() throws IOException, InterruptedException {

        Task task1 = new Task("Task 1", "Desc 1");
        Task task2 = new Task("Task 2", "Desc 2");
        Task task3 = new Task("Task 3", "Desc 3");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getTaskById(3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasksFromServerArray = gson.fromJson(response.body(), Task[].class);
        List<Task> tasksFromServer = List.of(tasksFromServerArray);

        assertNotNull(tasksFromServer, "История не должна быть null");
        assertEquals(3, tasksFromServer.size(), "Неверное количество задач в истории");

        assertEquals(task1.getId(), tasksFromServer.get(0).getId(), "Первая задача в истории неверна");
        assertEquals(task2.getId(), tasksFromServer.get(1).getId(), "Вторая задача в истории неверна");
        assertEquals(task3.getId(), tasksFromServer.get(2).getId(), "Третья задача в истории неверна");
    }


}

