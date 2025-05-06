import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected HashMap<Integer, Task> taskMap = new HashMap<>();
    protected HashMap<Integer, Epic> epicMap = new HashMap<>();
    protected HashMap<Integer, Subtask> subtaskMap = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int idCounter = 0;

    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }

    public boolean isOverlap(Task task1, Task task2) {
        if (isHaveTime(task1) && (isHaveTime(task2))) {
            LocalDateTime start1;
            LocalDateTime end1;
            LocalDateTime start2;
            LocalDateTime end2;
            if (isThisEpic(task1)) {
                start1 = ((Epic) task1).getEpicStartTime();
                end1 = ((Epic) task1).getEpicEndTime();
            } else {
                start1 = task1.getStartTime().get();
                end1 = task1.getEndTime().get();
            }
            if (isThisEpic(task2)) {
                start2 = ((Epic) task2).getEpicStartTime();
                end2 = ((Epic) task2).getEpicEndTime();
            } else {
                start2 = task2.getStartTime().get();
                end2 = task2.getEndTime().get();
            }


        } else return false;
    }

    public boolean isThisEpic(Task task) {
        if (task instanceof Epic) {
            return true;
        } else return false;
    }

    public boolean isHaveTime(Task task) {
        if (isThisEpic(task)) {
            if (((Epic) task).getEpicEndTime() != null &&
                    ((Epic) task).getEpicStartTime() != null &&
                    ((Epic) task).getEpicDuration() != Duration.ZERO) {
                return true;
            }
        } else if (task.getStartTime().isPresent() &&
                task.getEndTime().isPresent() &&
                !task.getDuration().get().isZero()) {
            return true;
        }
        return false;
    }

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
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epicMap.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtasksMapInEpic().values()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllTasks() {
        prioritizedTasks.removeAll(taskMap.values());
        taskMap.clear();
    }

    @Override
    public void deleteAllEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        prioritizedTasks.removeAll(subtaskMap.values());
        subtaskMap.clear();
        getEpics()
                .stream()
                .forEach(epic -> {
                    getStatusForEpic(epic.getId());
                    epic.getSubtasksMapInEpic().clear();
                    epic.calculateTimesEpic();
                });
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
        if (task.getStartTime().isPresent()) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(getNewId());
        epicMap.put(epic.getId(), epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epicMap.get(epicId);
        if (epicMap.containsKey(epicId) && (epic != null)) {
            subtask.setId(getNewId());
            subtaskMap.put(subtask.getId(), subtask);
            getEpicMapBySubtask(subtask).put(subtask.getId(), subtask);
            getStatusForEpic(epicId);
            epic.calculateTimesEpic();
            if (subtask.getStartTime().isPresent()) {
                prioritizedTasks.add(subtask);
            }
        }
    }

    @Override
    public boolean update(Task task) {
        int taskId = task.getId();
        Task oldTask = taskMap.get(taskId);
        if (oldTask != null) {
            prioritizedTasks.remove(oldTask);
            taskMap.put(taskId, task);
            if (task.getStartTime().isPresent()) {
                prioritizedTasks.add(task);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Subtask subtask) {
        Subtask oldSubtask = subtaskMap.get(subtask.getId());
        if (oldSubtask != null) {
            prioritizedTasks.remove(oldSubtask);
            int subTaskId = subtask.getId();
            Epic epicObject = epicMap.get(subtask.getEpicId());
            subtaskMap.put(subTaskId, subtask);
            getEpicMapBySubtask(subtask).put(subTaskId, subtask);
            getStatusForEpic(subtask.getEpicId());
            if (subtask.getStartTime().isPresent()) {
                prioritizedTasks.add(subtask);
            }
            epicObject.calculateTimesEpic();
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

    protected final Set<Task> prioritizedTasks = new TreeSet<>(new Comparator<Task>() {
        @Override
        public int compare(Task task1, Task task2) {
            if (task1.getStartTime().isPresent() && task2.getStartTime().isPresent()) {
                LocalDateTime time1 = task1.getStartTime().get();
                LocalDateTime time2 = task2.getStartTime().get();
                int timeCompare = time1.compareTo(time2);
                if (timeCompare == 0) {
                    return Integer.compare(task1.getId(), task2.getId());
                } else {
                    return timeCompare;
                }
            }
            if (task1.getStartTime().isPresent()) {
                return -1;
            } else if (task2.getStartTime().isPresent()) {
                return 1;
            } else {
                return Integer.compare(task1.getId(), task2.getId());
            }
        }
    });

    private boolean checkEpicInMap(Epic epic) {
        int epicId = epic.getId();
        if (epicMap.containsKey(epicId)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTaskById(int id) {
        Task task = taskMap.get(id);
        if (task != null) {
            prioritizedTasks.remove(task);
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
                prioritizedTasks.remove(subtask);
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
            prioritizedTasks.remove(subtask);
            Epic epic = epicMap.get(subtask.getEpicId());
            subtaskMap.remove(id);
            epic.getSubtasksMapInEpic().remove(id);
            getStatusForEpic(subtask.getEpicId());
            historyManager.remove(id);
            epic.calculateTimesEpic();
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

    private boolean isAllSubtaskDone(List<Subtask> listInEpic) {
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



