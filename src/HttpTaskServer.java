import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;


public class HttpTaskServer {
    private HttpServer server;
    private TaskManager taskManager;
    private final Gson gson;
    private static final int PORT = 8080;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;

        this.gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Epic.class, new EpicSerializer())
                .create();

        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(taskManager, gson));
        server.createContext("/subtask", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicsHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritizedTasks", new PrioritizedHandler(taskManager, gson));
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer taskServer  = new HttpTaskServer(taskManager);
        taskServer .start();
    }

    public void start() {
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("\nСервер на порту " + PORT + " остановлен");
    }

    public Gson getGson() {
        return this.gson;
    }

}
