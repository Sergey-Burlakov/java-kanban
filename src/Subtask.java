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

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId == this.getId()){
            return;
        }
        this.epicId = epicId;
    }

    public String toString() {
        String result = "Subtask{" +
                "epicId='" + epicId + '\'' +
                "name='" + getName() + '\'';
        if (getDescription() != null) {
            result = result + ", description.length=" + getDescription().length()+ '\'' +
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
