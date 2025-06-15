import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    TaskManager taskManager;
    Gson gson;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try {
            String[] requestArray = httpExchange.getRequestURI().getPath().split("/");
            String method = httpExchange.getRequestMethod();
            System.out.format("Началась обработка %S /%s запроса от клиента.\n", method, requestArray[1]);
            if (method.equals("GET")) {
                get(httpExchange, requestArray);
            } else {
                sendText(httpExchange, 405, "В запросе использован несуществующий метод!");
            }
        } catch (Throwable e) {
            sendText(httpExchange, 500, "Произошла ошибка на стороне сервера");
        }
    }

    private void get(HttpExchange httpExchange, String[] requestArray) throws IOException {
        if (requestArray.length == 2) {
            sendText(httpExchange, 200, gson.toJson(taskManager.getPrioritizedTasks()));
        } else sendErrorRequest(httpExchange);
    }

}
