import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected void sendText(HttpExchange httpExchange, int rCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(rCode, resp.length);
        httpExchange.getResponseBody().write(resp);
        httpExchange.close();
    }

    protected void sendNotFound(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(404, 0);
        httpExchange.close();
    }


    protected void sendErrorRequest(HttpExchange httpExchange) throws IOException {
        sendText(httpExchange, 400, "Ошибка в составлении запроса");
    }

    protected void sendNotFoundId(HttpExchange httpExchange, TaskType type, int id) throws IOException {
        String typeS = "N/A";
        switch (type) {
            case TASK -> typeS = "Задача";
            case EPIC -> typeS = "Эпик";
            case SUBTASK -> typeS = "Подзадача";
        }
        sendText(httpExchange, 404, String.format("%s ID=%d не найдена", typeS, id));
    }

    protected void sendIncorrectId(HttpExchange httpExchange, String id) throws IOException {
        sendText(httpExchange, 400, String.format("ID не может быть равен «%s»", id));
    }

    protected void sendHasInteractions(HttpExchange httpExchange, TaskOverlapException e) throws IOException {
        sendText(httpExchange, 406, e.getMessage());
    }
}
