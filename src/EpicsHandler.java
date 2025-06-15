import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {
    TaskManager taskManager;
    Gson gson;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
                String epicsJson = gson.toJson(taskManager.getEpics());
                sendText(httpExchange, 200, epicsJson);
            }
            case 3 -> {
                try {
                    id = Integer.parseInt(requestArray[2]);
                } catch (NumberFormatException e) {
                    sendIncorrectId(httpExchange, requestArray[2]);
                }
                try {
                    Epic epic = taskManager.getEpicById(id);
                    String epicById = gson.toJson(epic);
                    sendText(httpExchange, 200, epicById);
                } catch (NotFoundException e) {
                    sendNotFoundId(httpExchange, TaskType.EPIC, id);
                }
            }
            case 4 -> {
                if (requestArray[3].equals("subtasks")) {
                    try {
                        id = Integer.parseInt(requestArray[2]);
                    } catch (NumberFormatException e) {
                        sendIncorrectId(httpExchange, requestArray[2]);
                    }
                    try {
                        taskManager.getEpicById(id);
                    } catch (NotFoundException e) {
                        sendNotFoundId(httpExchange, TaskType.EPIC, id);
                        return;
                    }
                    sendText(httpExchange, 200,gson.toJson(taskManager.getEpicSubtasks(id)));
                }
            }
            default -> sendErrorRequest(httpExchange);
        }
    }

    private void post(HttpExchange httpExchange, String[] requestArray) throws IOException {
        if (requestArray.length == 2) {
            String epicSring = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Epic epic = gson.fromJson(epicSring, Epic.class);
            if (epic.getId() == 0) {
                createEpic(httpExchange, epic);
            } else updateEpic(httpExchange, epic);
        } else sendErrorRequest(httpExchange);
    }

    private void createEpic(HttpExchange httpExchange, Epic epic) throws IOException {
        try {
            taskManager.addEpic(epic);
        } catch (TaskOverlapException e) {
            sendHasInteractions(httpExchange, e);
            return;
        }
        String createdEpicJson = gson.toJson(epic);
        sendText(httpExchange, 200, createdEpicJson);
    }

    private void updateEpic(HttpExchange httpExchange, Epic epic) throws IOException {
        try {
            taskManager.update(epic);
        } catch (TaskOverlapException e) {
            sendHasInteractions(httpExchange, e);
            return;
        } catch (NotFoundException e) {
            sendNotFoundId(httpExchange, TaskType.EPIC, epic.getId());
            return;
        }
        String updatedEpicJson = gson.toJson(epic);
        sendText(httpExchange, 200, updatedEpicJson);
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
                taskManager.deleteEpicById(id);
            } catch (NotFoundException e) {
                sendNotFoundId(httpExchange, TaskType.EPIC, id);
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
