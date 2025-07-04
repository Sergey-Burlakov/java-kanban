import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> taskMap = new HashMap<>();
    protected final Map<Integer, Epic> epicMap = new HashMap<>();
    protected final Map<Integer, Subtask> subtaskMap = new HashMap<>();
    protected final Set<Task> prioritizedTasks = new TreeSet<>(new Comparator<Task>() {
        @Override
        public int compare(Task task1, Task task2) {
            LocalDateTime time1 = task1.getStartTime().get();
            LocalDateTime time2 = task2.getStartTime().get();
            int timeCompare = time1.compareTo(time2);
            if (timeCompare == 0) {
                return Integer.compare(task1.getId(), task2.getId());
            } else {
                return timeCompare;
            }
        }
    });
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int idCounter = 0;

    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }

    public boolean isOverlap(Task task1, Task task2) {
        if (task1.equals(task2) || !isHaveTime(task1) || !isHaveTime(task2)) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime().get();
        LocalDateTime end1 = task1.getEndTime().get();
        LocalDateTime start2 = task2.getStartTime().get();
        LocalDateTime end2 = task2.getEndTime().get();
        return !((end1.isBefore(start2) || end1.isEqual(start2)) || (start1.isAfter(end2) || start1.isEqual(end2)));
    }

    public boolean isOverlapAll(Task task) {
        if (!isHaveTime(task)) {
            return false;
        }
        return getPrioritizedTasks()
                .stream()
                .filter(taskFilt -> !taskFilt.equals(task))
                .anyMatch(taskFilt -> isOverlap(taskFilt, task));
    }

    public Optional<Task> whoIsOverlap(Task task) {
        if (!isHaveTime(task)) {
            return Optional.empty();
        }
        return getPrioritizedTasks()
                .stream()
                .filter(taskFilt -> !taskFilt.equals(task))
                .filter(taskFilt -> isOverlap(taskFilt, task))
                .findFirst();
    }

    public boolean isHaveTime(Task task) {
        return task.getStartTime().isPresent() &&
                task.getDuration().isPresent() &&
                !task.getDuration().get().isZero();
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
            return task;
        } else {
            throw new NotFoundException("Задача с id=" + id + " не найдена");
        }
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            historyManager.add(epic);
            return epic;
        } else {
            throw new NotFoundException("Эпик с id=" + id + " не найден");
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
            return subtask;
        } else {
            throw new NotFoundException("Подзадача с id=" + id + " не найдена");
        }
    }

    @Override
    public void addTask(Task task) {
        Optional<Task> overLapTask = whoIsOverlap(task);
        if (overLapTask.isPresent()) {
            generateTaskOverlapException(ContextOperation.ADD, task, overLapTask.get());
        }
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
        Optional<Task> overLapSubtask = whoIsOverlap(subtask);
        if (overLapSubtask.isPresent()) {
            generateTaskOverlapException(ContextOperation.ADD, subtask, overLapSubtask.get());
        }
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
        Optional<Task> overLapTask = whoIsOverlap(task);
        if (overLapTask.isPresent()) {
            generateTaskOverlapException(ContextOperation.UPDATE, task, overLapTask.get());
        }
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
        Optional<Task> overLapSubtask = whoIsOverlap(subtask);
        if (overLapSubtask.isPresent()) {
            generateTaskOverlapException(ContextOperation.UPDATE, subtask, overLapSubtask.get());
        }
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
        Epic existingEpic = epicMap.get(epic.getId());
        if (existingEpic != null) {
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            getStatusForEpic(existingEpic.getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTaskById(int id) {
        Task task = taskMap.get(id);
        if (task == null) {
            throw new NotFoundException("Задача с id=" + id + " не найдена");
        }
        prioritizedTasks.remove(task);
        taskMap.remove(id);
        historyManager.remove(id);
        return true;
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
        } else throw new NotFoundException("Эпик с id=" + id + " не найден");
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
        } else throw new NotFoundException("Подзадача с id=" + id + " не найдена");
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

    private void generateTaskOverlapException(ContextOperation context, Task addedTask, Task taskOverlap) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");
        String mutable = "";
        switch (context) {
            case ADD -> mutable = "добавить";
            case UPDATE -> mutable = "обновить";
        }
        final String contextS = mutable;
        throw new TaskOverlapException(String.format(
                "Невозможно %s задачу «%s» ID=%d. Обнаружено пересечение по времени с задачью «%s» ID=%d\n" +
                        "Время задачи ID=%d: с %s по %s\n" +
                        "Время задачи ID=%d: с %s по %s",
                contextS,
                addedTask.getName(),
                addedTask.getId(),
                taskOverlap.getName(),
                taskOverlap.getId(),
                addedTask.getId(),
                addedTask.getStartTime().map(dt -> dt.format(dateTimeFormatter)).orElse("N/A"),
                addedTask.getEndTime().map(dt -> dt.format(dateTimeFormatter)).orElse("N/A"),
                taskOverlap.getId(),
                taskOverlap.getStartTime().map(dt -> dt.format(dateTimeFormatter)).orElse("N/A"),
                taskOverlap.getEndTime().map(dt -> dt.format(dateTimeFormatter)).orElse("N/A")
        ));
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
        return size == cheker;
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

    private enum ContextOperation {
        ADD,
        UPDATE
    }
}



