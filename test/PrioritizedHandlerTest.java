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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PrioritizedHandlerTest {
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldReturnPrioritizedTasksSortedByStartTime() throws IOException, InterruptedException {

        // Эта задача будет в середине списка
        Task taskA = new Task("Задача A", "На сегодня",
                LocalDateTime.now(), Duration.ofHours(1));
        manager.addTask(taskA);

        // Эта задача будет в конце списка (самая поздняя)
        Task taskB_later = new Task("Задача B", "На завтра",
                LocalDateTime.now().plusDays(1), Duration.ofHours(1));
        manager.addTask(taskB_later);

        // Эта задача будет в начале списка (самая ранняя)
        Task taskC_earlier = new Task("Задача C", "Была вчера",
                LocalDateTime.now().minusDays(1), Duration.ofHours(1));
        manager.addTask(taskC_earlier);

        // Эта задача НЕ должна попасть в список, т.к. у нее нет времени
        Task taskD_noTime = new Task("Задача D", "Без времени");
        manager.addTask(taskD_noTime);

        // 2. Отправка запроса
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritizedTasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");

        Task[] tasksFromServerArray = gson.fromJson(response.body(), Task[].class);
        List<Task> prioritizedTasks = List.of(tasksFromServerArray);

        assertNotNull(prioritizedTasks, "Список приоритетных задач не должен быть null");
        assertEquals(3, prioritizedTasks.size(), "В списке должно быть 3 задачи (задача без времени игнорируется)");

        // Проверяем ПРАВИЛЬНЫЙ ПОРЯДОК СОРТИРОВКИ
        assertEquals(taskC_earlier.getId(), prioritizedTasks.get(0).getId(), "Первой должна быть самая ранняя задача");
        assertEquals(taskA.getId(), prioritizedTasks.get(1).getId(), "Второй должна быть средняя задача");
        assertEquals(taskB_later.getId(), prioritizedTasks.get(2).getId(), "Третьей должна быть самая поздняя задача");
    }
}