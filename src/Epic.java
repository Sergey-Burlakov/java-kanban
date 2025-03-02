import java.util.HashMap;

public class Epic extends Task{

    private HashMap <Integer, Subtask> subtasksMapInEpic = new HashMap<>();
    public Epic(String name)
    {
        super(name);
    }
    public Epic(String name, String description){
        super(name, description);
    }


    public HashMap<Integer, Subtask> getSubtasksMapInEpic() {
        return subtasksMapInEpic;
    }

    public void setSubtasksMapInEpic(HashMap<Integer, Subtask> subtasksMapInEpic) {
        this.subtasksMapInEpic = subtasksMapInEpic;
    }
}
