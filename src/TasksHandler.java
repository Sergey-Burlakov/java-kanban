import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    TaskManager taskManager;
    Gson gson;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String[] requestArray = httpExchange.getRequestURI().getPath().split("/");
            String method = httpExchange.getRequestMethod();
            System.out.format("Началась обработка %S /%s запроса от клиента.", method, requestArray[1]);
            switch (method) {
                case "GET" -> get(httpExchange, requestArray);
                case "POST" -> post(httpExchange, requestArray);
                case "DELETE" -> delete(httpExchange, requestArray);
                default -> sendText(httpExchange, 405, "В запросе использован несуществующий метод!");
            }
        } catch (Throwable e) {
            sendText(httpExchange, 500, "Произошла ошибка на стороне сервера");
        }
    }

    private void get(HttpExchange httpExchange, String[] requestArray) throws IOException {
        int id = -1;
        switch (requestArray.length) {
            case 2 -> {
                String tasksJson = gson.toJson(taskManager.getTasks());
                sendText(httpExchange, 200, tasksJson);
            }
            case 3 -> {
                try {
                    id = Integer.parseInt(requestArray[2]);
                } catch (NumberFormatException e) {
                    sendIncorrectId(httpExchange, requestArray[2]);
                }
                try {
                    Task task = taskManager.getTaskById(id);
                    String taskById = gson.toJson(task);
                    sendText(httpExchange, 200, taskById);
                } catch (NotFoundException e) {
                    sendNotFoundId(httpExchange, TaskType.TASK, id);
                }
            }
            default -> sendErrorRequest(httpExchange);
        }
    }

    private void post(HttpExchange httpExchange, String[] requestArray) throws IOException {
        if (requestArray.length == 2) {
            String taskSring = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Task task = gson.fromJson(taskSring, Task.class);
            if (task.getId() == 0) {
                createTask(httpExchange, task);
            } else updateTask(httpExchange, task);
        } else sendErrorRequest(httpExchange);
    }

    private void createTask(HttpExchange httpExchange, Task task) throws IOException {
        try {
            taskManager.addTask(task);
        } catch (TaskOverlapException e) {
            sendHasInteractions(httpExchange, e);
            return;
        }
        String createdTaskJson = gson.toJson(task);
        sendText(httpExchange, 200, createdTaskJson);
    }

    private void updateTask(HttpExchange httpExchange, Task task) throws IOException {
        try {
            taskManager.update(task);
        } catch (TaskOverlapException e) {
            sendHasInteractions(httpExchange, e);
            return;
        } catch (NotFoundException e) {
            sendNotFoundId(httpExchange, TaskType.TASK, task.getId());
            return;
        }
        String updatedTaskJson = gson.toJson(task);
        sendText(httpExchange, 200, updatedTaskJson);
    }

    private void delete(HttpExchange httpExchange, String[] requestArray) throws IOException {
        if (requestArray.length != 3) {
            sendErrorRequest(httpExchange);
            return;
        }
        Optional<Integer> optionalId = idParse(requestArray[2]);
        if (optionalId.isPresent()) {
            int id = optionalId.get();
            try {
                taskManager.deleteTaskById(id);
            } catch (NotFoundException e) {
                sendNotFoundId(httpExchange, TaskType.TASK, id);
                return;
            }
            httpExchange.sendResponseHeaders(201, 0);
        } else {
            sendErrorRequest(httpExchange);
        }
    }

    private Optional<Integer> idParse(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
