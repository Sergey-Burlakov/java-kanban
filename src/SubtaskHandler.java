import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SubtaskHandler extends BaseHttpHandler {
    TaskManager taskManager;
    Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
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
                String subtaskJson = gson.toJson(taskManager.getSubtasks());
                sendText(httpExchange, 200, subtaskJson);
            }
            case 3 -> {
                try {
                    id = Integer.parseInt(requestArray[2]);
                } catch (NumberFormatException e) {
                    sendIncorrectId(httpExchange, requestArray[2]);
                }
                try {
                    Subtask subtask = taskManager.getSubtaskById(id);
                    String subtaskById = gson.toJson(subtask);
                    sendText(httpExchange, 200, subtaskById);
                } catch (NotFoundException e) {
                    sendNotFoundId(httpExchange, TaskType.SUBTASK, id);
                }
            }
            default -> sendErrorRequest(httpExchange);
        }
    }

    private void post(HttpExchange httpExchange, String[] requestArray) throws IOException {
        if (requestArray.length == 2) {
            String subtaskSring = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Subtask subtask = gson.fromJson(subtaskSring, Subtask.class);
            if (subtask.getId() == 0) {
                createSubtask(httpExchange, subtask);
            } else updateSubtask(httpExchange, subtask);
        } else sendErrorRequest(httpExchange);
    }

    private void createSubtask(HttpExchange httpExchange, Subtask subtask) throws IOException {
        try {
            taskManager.getEpicById(subtask.getEpicId());
            try {
                taskManager.addSubtask(subtask);
            } catch (TaskOverlapException e) {
                sendHasInteractions(httpExchange, e);
                return;
            }
            String createdSubtaskJson = gson.toJson(subtask);
            sendText(httpExchange, 200, createdSubtaskJson);
        } catch (NotFoundException e) {
            sendNotFoundEpicId(httpExchange, subtask.getEpicId());
        }
    }

    private void updateSubtask(HttpExchange httpExchange, Subtask subtask) throws IOException {
        try {
            taskManager.getEpicById(subtask.getEpicId());
            try {
                taskManager.update(subtask);
            } catch (TaskOverlapException e) {
                sendHasInteractions(httpExchange, e);
                return;
            } catch (NotFoundException e) {
                sendNotFoundId(httpExchange, TaskType.SUBTASK, subtask.getId());
                return;
            }
            String updatedSubtaskJson = gson.toJson(subtask);
            sendText(httpExchange, 200, updatedSubtaskJson);
        } catch (NotFoundException e) {
            sendNotFoundEpicId(httpExchange, subtask.getEpicId());
        }
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
                taskManager.deleteSubtaskById(id);
            } catch (NotFoundException e) {
                sendNotFoundId(httpExchange, TaskType.SUBTASK, id);
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

    private void sendNotFoundEpicId(HttpExchange httpExchange, int id) throws IOException {
        sendText(httpExchange, 404, String.format("Не удалось обнаружить epicId=%d указанный в подзадаче", id));
    }
}
