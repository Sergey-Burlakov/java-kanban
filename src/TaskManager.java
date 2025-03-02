import java.util.HashMap;
import java.util.ArrayList;
import java.util.Objects;

public class TaskManager {
    private HashMap<Integer, Task> taskMap = new HashMap<>();
    private HashMap<Integer, Epic> epicMap = new HashMap<>();
    private HashMap<Integer, Subtask> subtaskMap = new HashMap<>();


    static int idCounter = 0;

    static int getNewId() {
        return ++idCounter;
    }


    //получение списка всех задач

    ArrayList<Task> getAllTasks() {
        ArrayList<Task> listTasks = new ArrayList<>();
        for (Task object : this.taskMap.values()) {
            listTasks.add(object);
        }
        return listTasks;
    }

    ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> listEpics = new ArrayList<>();
        for (Epic object : this.epicMap.values()) {
            listEpics.add(object);
        }
        return listEpics;
    }

    ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> listSubtasks = new ArrayList<>();
        for (Subtask object : this.subtaskMap.values()) {
            listSubtasks.add(object);
        }
        return listSubtasks;
    }

    ArrayList<Subtask> getAllSubtasksInEpic(int epicId) {
        Epic epic = getByIdEpic(epicId);
        ArrayList<Subtask> listSubtasksInEpic = new ArrayList<>();
        if (epic == null) {
            return listSubtasksInEpic;
        }
        HashMap<Integer, Subtask> mapInEpic = epic.getSubtasksMapInEpic();
        for (Subtask object : mapInEpic.values()) {
            listSubtasksInEpic.add(object);
        }
        return listSubtasksInEpic;
    }


    //удаление всех задач
    void deleteAllTasks() {
        taskMap.clear();
    }

    void deleteAllEpic() {
        epicMap.clear();
    }

    void deleteAllSubtask() {
        for (Epic epic : (getAllEpics())) {
            epic.getSubtasksMapInEpic().clear();
        }
        subtaskMap.clear();
    }

    //получение по id
    Task getByIdTask(int id) {
        return taskMap.get(id);
    }

    Epic getByIdEpic(int id) {
        return epicMap.get(id);
    }

    Subtask getByIdSubtask(int id) {
        return subtaskMap.get(id);
    }

    void addMap(Task task) {
        taskMap.put(task.getId(), task);
    }
    void addMap(Epic epic) {
        epicMap.put(epic.getId(), epic);
    }
    void addMap(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epicObject = epicMap.get(epicId);
        if (epicMap.containsKey(epicId) && (epicObject != null)) {
            subtaskMap.put(subtask.getId(), subtask);
            getEpicMapBySubtask(subtask).put(subtask.getId(), subtask);
            getStatusForEpic(epicId);
        }
    }

    //Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.

    boolean update(Task task) {
        if (checkTaskInMap(task)) {
            int taskId = task.getId();
            taskMap.put(taskId, task);
            return true;
        }
        return false;
    }

    boolean update(Epic epic) {
        if (checkEpicInMap(epic)) {
            int epicId = epic.getId();
            epicMap.put(epicId, epic);
            getStatusForEpic(epicId);
            return true;
        }
        return false;
    }

    boolean update(Subtask subtask) {
        if (checkSubtaskInMap(subtask)) {
            int subTaskId = subtask.getId();
            subtaskMap.put(subTaskId, subtask);
            getEpicMapBySubtask(subtask).put(subTaskId, subtask);
            getStatusForEpic(subtask.getEpicId());
            return true;
        }
        return false;
    }

    boolean checkTaskInMap(Task task) {
        int taskId = task.getId();
        if (taskMap.containsKey(taskId)) {
            return true;
        }
        return false;
    }

    boolean checkEpicInMap(Epic epic) {
        int epicId = epic.getId();
        if (epicMap.containsKey(epicId)) {
            return true;
        }
        return false;
    }

    boolean checkSubtaskInMap(Subtask subtask) {
        int subtaskId = subtask.getId();
        if (subtaskMap.containsKey(subtaskId) &&
                getEpicMapBySubtask(subtask).containsKey(subtaskId)) {
            return true;
        }
        return false;
    }


    //Удаление по идентификатору.
    boolean deletebyIdTask(int id) {
        if (taskMap.containsKey(id)) {
            taskMap.remove(id);
            return true;
        }
        return false;
    }

    boolean deletebyIdEpic(int id) {
        if (epicMap.containsKey(id)) {
            epicMap.remove(id);
            return true;
        }
        return false;
    }

    boolean deletebyIdSubtask(int id) {
        //получить объект Subtask
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null && getEpicMapBySubtask(subtask) != null) {
            subtaskMap.remove(id);
            getEpicMapBySubtask(subtask).remove(id);
            return true;
        }
        return false;
    }

    HashMap<Integer, Subtask> getEpicMapBySubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epicMap.containsKey(epicId)) {
            return null;
        } else {
            Epic epic = epicMap.get(epicId);
            if (epic != null) {
                HashMap<Integer, Subtask> mapInEpic = epic.getSubtasksMapInEpic();
                return mapInEpic;
            }
            return null;
        }
    }

    //назначить статус эпику
    void getStatusForEpic(int epicId) {
        Epic epic = epicMap.get(epicId);
        HashMap<Integer, Subtask> mapInEpic = epic.getSubtasksMapInEpic();
        if (mapInEpic.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else if (isAllSubtaskDone(getAllSubtasksInEpic(epicId))) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    boolean isAllSubtaskDone(ArrayList<Subtask> listInEpic) {
        int size = listInEpic.size();
        int cheker = 0;
        for (Subtask sub : listInEpic) {
            if (sub.getStatus() == Status.DONE) {
                ++cheker;
            }
        }
        if (size == cheker) {
            return true;
        } else return false;
    }

    @Override
    public String toString() {
        return "TaskManager{" +
                "taskMap=" + taskMap +
                ", epicMap=" + epicMap +
                ", subtaskMap=" + subtaskMap +
                '}';
    }
}



