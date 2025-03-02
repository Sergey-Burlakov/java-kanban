import java.util.HashMap;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int epicId, String name) {
        super(name);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description, String status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int epicId, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }


    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }


    public String toString() {
        String result = "Subtask{{" +
                "epicId='" + epicId + '\'' +
                "name='" + name + '\'';
        if (description != null) {
            result = result + ", description.length=" + description.length();
        } else {
            result = result + ", description=null'" + '\'' +
                    ", status=" + status +
                    ", id=" + id;
        }
        return result + "}";
    }
}
