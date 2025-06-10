import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int epicId, String name) {
        super(name);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, LocalDateTime startTime, Duration duration) {
        super(name, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId == this.getId()) {
            return;
        }
        this.epicId = epicId;
    }

    public String toString() {
        String result = "Subtask{" +
                "epicId='" + epicId + '\'' +
                "name='" + getName() + '\'';
        if (getDescription() != null) {
            result = result + ", description.length=" + getDescription().length() + '\'' +
                    ", status=" + getStatus() +
                    ", id=" + getId();
        } else {
            result = result + ", description=null'" + '\'' +
                    ", status=" + getStatus() +
                    ", id=" + getId();
        }
        return result + "}";
    }
}
