import java.util.HashMap;
import java.util.ArrayList;
public class TaskManager {
    private HashMap<Integer, Task> taskMap = new HashMap<>();
    private HashMap<Integer, Epic> epicMap = new HashMap<>();
    private HashMap<Integer, Subtask> subtaskMap = new HashMap<>();
    private int idCounter = 0;
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(this.taskMap.values());
    }
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(this.epicMap.values());
    }
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(this.subtaskMap.values());
    }
    public ArrayList<Subtask> getAllSubtasksInEpic(int epicId) {
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
    public void deleteAllTasks() {
        taskMap.clear();
    }
    public void deleteAllEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }
    public void deleteAllSubtasks() {
        for (Epic epic : (getAllEpics())) {
            epic.getSubtasksMapInEpic().clear();
            epic.setStatus(Status.NEW);
        }
        subtaskMap.clear();
    }
    public Task getByIdTask(int id) {
        return taskMap.get(id);
    }
    public Epic getByIdEpic(int id) {
        return epicMap.get(id);
    }
    public Subtask getByIdSubtask(int id) {
        return subtaskMap.get(id);
    }
    public void addTask(Task task) {
        task.setId(getNewId());
        taskMap.put(task.getId(), task);
    }
    public void addEpic(Epic epic) {
        epic.setId(getNewId());
        epicMap.put(epic.getId(), epic);
    }
    public void addSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epicObject = epicMap.get(epicId);
        if (epicMap.containsKey(epicId) && (epicObject != null)) {
            subtask.setId(getNewId());
            subtaskMap.put(subtask.getId(), subtask);
            getEpicMapBySubtask(subtask).put(subtask.getId(), subtask);
            getStatusForEpic(epicId);
        }
    }
    public boolean update(Task task) {
        if (checkTaskInMap(task)) {
            int taskId = task.getId();
            taskMap.put(taskId, task);
            return true;
        }
        return false;
    }
    public boolean update(Epic epic) {
        if (checkEpicInMap(epic)) {
            int epicId = epic.getId();
            epicMap.put(epicId, epic);
            getStatusForEpic(epicId);
            return true;
        }
        return false;
    }
    public boolean update(Subtask subtask) {
        if (checkSubtaskInMap(subtask)) {
            int subTaskId = subtask.getId();
            subtaskMap.put(subTaskId, subtask);
            getEpicMapBySubtask(subtask).put(subTaskId, subtask);
            getStatusForEpic(subtask.getEpicId());
            return true;
        }
        return false;
    }
    private boolean checkTaskInMap(Task task) {
        int taskId = task.getId();
        if (taskMap.containsKey(taskId)) {
            return true;
        }
        return false;
    }
    private boolean checkEpicInMap(Epic epic) {
        int epicId = epic.getId();
        if (epicMap.containsKey(epicId)) {
            return true;
        }
        return false;
    }
    private boolean checkSubtaskInMap(Subtask subtask) {
        int subtaskId = subtask.getId();
        if (subtaskMap.containsKey(subtaskId) &&
                getEpicMapBySubtask(subtask).containsKey(subtaskId)) {
            return true;
        }
        return false;
    }
    public boolean deletebyIdTask(int id) {
        if (taskMap.containsKey(id)) {
            taskMap.remove(id);
            return true;
        }
        return false;
    }
    public boolean deletebyIdEpic(int id) {
        if (epicMap.containsKey(id)) {
            epicMap.remove(id);
            for (Subtask subtask : getAllSubtasksInEpic(id)){
                subtaskMap.remove(subtask.getId());
                getEpicMapBySubtask(subtask).remove(subtask.getId());
            }
            return true;
        }
        return false;
    }
    public boolean deletebyIdSubtask(int id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null && getEpicMapBySubtask(subtask) != null) {
            subtaskMap.remove(id);
            getEpicMapBySubtask(subtask).remove(id);
            getStatusForEpic(subtask.getEpicId());
            return true;
        }
        return false;
    }
    private HashMap<Integer, Subtask> getEpicMapBySubtask(Subtask subtask) {
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
    private void getStatusForEpic(int epicId) {
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
    private boolean isAllSubtaskDone(ArrayList<Subtask> listInEpic) {
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
    private int getNewId() {
        return ++idCounter;
    }
}



