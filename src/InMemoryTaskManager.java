import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected HashMap<Integer, Task> taskMap = new HashMap<>();
    protected HashMap<Integer, Epic> epicMap = new HashMap<>();
    protected HashMap<Integer, Subtask> subtaskMap = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int idCounter = 0;

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(this.taskMap.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(this.epicMap.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(this.subtaskMap.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = getEpicById(epicId);
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

    @Override
    public void deleteAllTasks() {
        taskMap.clear();
    }

    @Override
    public void deleteAllEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : (getEpics())) {
            epic.getSubtasksMapInEpic().clear();
            epic.setStatus(Status.NEW);
        }
        subtaskMap.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = taskMap.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void addTask(Task task) {
        task.setId(getNewId());
        taskMap.put(task.getId(), task);
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(getNewId());
        epicMap.put(epic.getId(), epic);
    }

    @Override
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

    @Override
    public boolean update(Task task) {
        if (checkTaskInMap(task)) {
            int taskId = task.getId();
            taskMap.put(taskId, task);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Epic epic) {
        if (checkEpicInMap(epic)) {
            int epicId = epic.getId();
            epicMap.put(epicId, epic);
            getStatusForEpic(epicId);
            return true;
        }
        return false;
    }

    @Override
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

    @Override
    public boolean deleteTaskById(int id) {
        if (taskMap.containsKey(id)) {
            taskMap.remove(id);
            historyManager.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteEpicById(int id) {
        if (epicMap.containsKey(id)) {
            for (Subtask subtask : getEpicSubtasks(id)) {
                subtaskMap.remove(subtask.getId());
                getEpicMapBySubtask(subtask).remove(subtask.getId());
                historyManager.remove(subtask.getId());
            }
            epicMap.remove(id);
            historyManager.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null && getEpicMapBySubtask(subtask) != null) {
            subtaskMap.remove(id);
            getEpicMapBySubtask(subtask).remove(id);
            getStatusForEpic(subtask.getEpicId());
            historyManager.remove(id);
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

    protected void getStatusForEpic(int epicId) {
        Epic epic = epicMap.get(epicId);
        HashMap<Integer, Subtask> mapInEpic = epic.getSubtasksMapInEpic();
        if (mapInEpic.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else if (isAllSubtaskDone(getEpicSubtasks(epicId))) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
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

    protected void setIdCounter(int idCounter) {
        this.idCounter = idCounter;
    }
}



