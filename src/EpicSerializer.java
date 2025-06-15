import com.google.gson.*;

import java.lang.reflect.Type;

public class EpicSerializer implements JsonSerializer<Epic> {
    @Override
    public JsonElement serialize(Epic epic, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject result = new JsonObject();
        result.addProperty("name", epic.getName());
        result.addProperty("description", epic.getDescription());
        result.addProperty("id", epic.getId());
        result.addProperty("status", epic.getStatus().toString());
        result.add("startTime", context.serialize(epic.getStartTime().orElse(null)));
        result.add("duration", context.serialize(epic.getDuration().orElse(null)));
        result.add("endTime", context.serialize(epic.getEndTime().orElse(null)));

        JsonArray subtaskIds = new JsonArray();
        if (epic.getSubtasksMapInEpic() != null) {
            for (Subtask subtask : epic.getSubtasksMapInEpic().values()) {
                subtaskIds.add(subtask.getId());
            }
        }

        result.add("subtaskIds", subtaskIds);
        return result;
    }
}
