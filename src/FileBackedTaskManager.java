import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileTaskManager = new FileBackedTaskManager(file);
        int maxID = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                Task task = fromString(line);
                if (task.getId() > maxID) {
                    maxID = task.getId();
                }
                fileTaskManager.addTasksInRAM(task);
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка загрузки из файла " + file.getPath(), e);
        }
        fileTaskManager.addEnterEpics();
        fileTaskManager.setIdCounter(maxID);
        return fileTaskManager;
    }

    private static Task fromString(String value) {
        String[] taskArray = value.split(",", -1);
        int id = Integer.parseInt(taskArray[0]);
        TaskType type = TaskType.valueOf(taskArray[1].toUpperCase());
        String nameString = taskArray[2];
        Status status = Status.valueOf(taskArray[3].toUpperCase());
        String descriptionString = taskArray[4];
        String startTimeString = taskArray[6];
        String durationString = taskArray[7];

        Task taskObject = null;
        switch (type) {
            case TASK -> taskObject = new Task(nameString, descriptionString);
            case EPIC -> taskObject = new Epic(nameString, descriptionString);
            case SUBTASK -> {
                int epicId = Integer.parseInt(taskArray[5]);
                taskObject = new Subtask(epicId, nameString, descriptionString);
            }
        }
        if (!startTimeString.isEmpty() && (type == TaskType.TASK || type == TaskType.SUBTASK)) {
            taskObject.setStartTime(LocalDateTime.parse(startTimeString, FileBackedTaskManager.DATE_TIME_FORMATTER));
        }
        if (!durationString.isEmpty() && (type == TaskType.TASK || type == TaskType.SUBTASK)) {
            taskObject.setDuration(Duration.ofMinutes(Long.parseLong(durationString)));
        }
        taskObject.setId(id);
        if (type != TaskType.EPIC) {
            taskObject.setStatus(status);
        }
        return taskObject;
    }

    private static String taskToString(Task task) {
        String epicIdCsv = "";
        String startTimeStr = "";
        String durationStr = "";
        String type = switch (task) {
            case Subtask s -> "SUBTASK";
            case Epic e -> "EPIC";
            case Task t -> "TASK";
        };
        String descriptionForCsv = task.getDescription() == null ? "" : task.getDescription();
        if (task instanceof Subtask s) {
            epicIdCsv = String.valueOf(s.getEpicId());
        }
        if (task.getStartTime().isPresent()) {
            startTimeStr = task.getStartTime().get().format(FileBackedTaskManager.DATE_TIME_FORMATTER);
        }
        if (task.getDuration().isPresent()) {
            durationStr = String.valueOf(task.getDuration().get().toMinutes());
        }
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s", task.getId(), type, task.getName(), task.getStatus(),
                descriptionForCsv, epicIdCsv, startTimeStr, durationStr);
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public boolean deleteTaskById(int id) {
        if (super.deleteTaskById(id)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteEpicById(int id) {
        if (super.deleteEpicById(id)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteSubtaskById(int id) {
        if (super.deleteSubtaskById(id)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Task task) {
        if (super.update(task)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Epic epic) {
        if (super.update(epic)) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Subtask subtask) {
        if (super.update(subtask)) {
            save();
            return true;
        }
        return false;
    }

    private void addEnterEpics() {
        for (Epic epic : super.epicMap.values()) {
            for (Subtask subtask : super.subtaskMap.values()) {
                if (subtask.getEpicId() == epic.getId()) {
                    epic.getSubtasksMapInEpic().put(subtask.getId(), subtask);
                }
            }
            super.getStatusForEpic(epic.getId());
            epic.calculateTimesEpic();
        }
    }

    private void addTasksInRAM(Task task) {
        int id = task.getId();
        if (task instanceof Epic epic) {
            super.epicMap.put(id, epic);
        } else if (task instanceof Subtask subtask) {
            super.subtaskMap.put(id, subtask);
        } else {
            super.taskMap.put(id, task);
        }
        if (task.getStartTime().isPresent()) {
            prioritizedTasks.add(task);
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,startTime,durationMinutes\n");
            List<Task> tasksList = getTasks();
            List<Epic> epicList = getEpics();
            List<Subtask> subtasksList = getSubtasks();
            for (Task enter : tasksList) {
                writer.write(taskToString(enter) + "\n");
            }
            for (Epic enter : epicList) {
                writer.write(taskToString(enter) + "\n");
            }
            for (Subtask enter : subtasksList) {
                writer.write(taskToString(enter) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл " + file.getPath(), e);
        }
    }
}
