import java.util.ArrayList;
public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Subtask> getEpicSubtasks(int epicId);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    boolean update(Task task);

    boolean update(Epic epic);

    boolean update(Subtask subtask);

    boolean deleteTaskById(int id);

    boolean deleteEpicById(int id);

    boolean deleteSubtaskById(int id);

}
