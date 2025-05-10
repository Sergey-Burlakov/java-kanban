import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void addTaskAndGetItById() {
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration = Duration.ofMinutes(120);

        Task newTask = new Task("Имя задачи", "Описание задачи", startTime, duration);
        taskManager.addTask(newTask);
        Task gettedTask = taskManager.getTaskById(newTask.getId());

        assertNotNull(gettedTask, "Задача не должна быть null после добавления и получения");
        assertEquals(newTask, gettedTask, "id задач не совпадает");
        assertEquals(newTask.getName(), gettedTask.getName(), "Имя задачи не совпадает");
        assertEquals(newTask.getDescription(), gettedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(newTask.getStatus(), gettedTask.getStatus(), "Статусы не совпадают");
        assertEquals(newTask.getStartTime(), gettedTask.getStartTime(), "Время старта не совпадает");
        assertEquals(newTask.getDuration(), gettedTask.getDuration(), "Продолжительность задачи не совпадает");
        assertEquals(newTask.getEndTime(), gettedTask.getEndTime(), "Время окончания не совпадает");


    }

    @Test
    void addTaskAndGetItByIdWithoutTime() {
        Task newTask = new Task("Имя задачи", "Описание задачи");
        taskManager.addTask(newTask);
        Task gettedTask = taskManager.getTaskById(newTask.getId());
        assertEquals(newTask.getEndTime(), gettedTask.getEndTime(), "Ошибка получения Optional.empty в " +
                "getEndTime");
        assertEquals(newTask.getStartTime(), gettedTask.getStartTime(), "Ошибка получения Optional.empty в " +
                "getStartTime");
        assertEquals(newTask.getDuration(), gettedTask.getDuration(), "Ошибка получения Optional.empty в " +
                "getDuration");
        assertNotNull(gettedTask, "Задача не должна быть null");
        assertEquals(newTask.getId(), gettedTask.getId(), "ID задачи не совпадает");
        assertEquals(newTask.getName(), gettedTask.getName(), "Имя задачи не совпадает");
        assertEquals(newTask.getDescription(), gettedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(Status.NEW, gettedTask.getStatus(), "Статус по умолчанию должен быть NEW");

    }

    @Test
    void testUpdate() {
        LocalDateTime originalStartTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration originalDuration = Duration.ofMinutes(120);

        Task originalTask = new Task("Имя задачи", "Описание задачи", originalStartTime,
                originalDuration);
        taskManager.addTask(originalTask);
        LocalDateTime modifiedStartTime = LocalDateTime.of(2025, 5, 10, 11, 45);
        Duration modifiedDuration = Duration.ofMinutes(120);
        Task updateTask = new Task("Другое имя", "Другое описание", Status.IN_PROGRESS,
                modifiedStartTime, modifiedDuration);
        updateTask.setId(originalTask.getId());

        boolean isUpdated = taskManager.update(updateTask);
        assertTrue(isUpdated, "Метод update должен вернуть true для существующей задачи");
        Task taskAfterUpdate = taskManager.getTaskById(originalTask.getId());
        assertNotNull(taskAfterUpdate, "Задача не должна быть null после обновления");
        assertEquals(updateTask.getId(), taskAfterUpdate.getId(), "ID не должен измениться после обновления");
        assertEquals(updateTask.getName(), taskAfterUpdate.getName(), "Имя должно было обновиться");
        assertEquals(updateTask.getDescription(), taskAfterUpdate.getDescription(), "Описание должно было " +
                "обновиться");
        assertEquals(updateTask.getStatus(), taskAfterUpdate.getStatus(), "Статус должен был обновиться");
        assertEquals(updateTask.getStartTime(), taskAfterUpdate.getStartTime(), "Время начала должно было " +
                "обновиться");
        assertEquals(updateTask.getDuration(), taskAfterUpdate.getDuration(), "Продолжительность должна " +
                "была обновиться");
        assertEquals(updateTask.getEndTime(), taskAfterUpdate.getEndTime(), "Время окончания должно было " +
                "обновиться");
    }

    @Test
    void shouldReturnFalseWhenUpdatingAnUnaddedTask() {
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration = Duration.ofMinutes(120);
        Task task = new Task("Имя задачи", "Описание задачи", startTime, duration);
        task.setId(99999);
        assertFalse(taskManager.update(task), "Над задачей не было совершено операции ADD, но при ее " +
                "обновлении вернуло True");
    }

    @Test
    void shouldReturnTrueWhenTheTaskIsDeleted() {
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration = Duration.ofMinutes(120);
        Task task = new Task("Имя задачи", "Описание задачи", startTime, duration);
        taskManager.addTask(task);
        int taskId = task.getId();
        assertTrue(taskManager.deleteTaskById(task.getId()), "Удаление задачи вернуло False");
        assertFalse(taskManager.deleteTaskById(task.getId()), "Удаление удаленной задачи вернуло True");
        assertTrue(taskManager.getPrioritizedTasks().stream().noneMatch(t -> t.getId() == taskId),
                "Удаленная задача не должна присутствовать в списке приоритетных задач");
    }

    @Test
    void shouldReturnFalseWhenDeletingANonexistentTask() {
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration = Duration.ofMinutes(120);
        Task task = new Task("Имя задачи", "Описание задачи", startTime, duration);
        taskManager.addTask(task);
        assertFalse(taskManager.deleteTaskById(Integer.MAX_VALUE), "Вернуло True при удалении " +
                "несуществующего ID");
    }

    @Test
    void shouldReturnEmptyListWhenNoTasksAdded() {
        assertNotNull(taskManager.getTasks(),"При возвращении пустого списка задач вернуло null объект");
        assertTrue(taskManager.getTasks().isEmpty(),"Возвращает False при проверке пустого листа задач");

    }

    @Test
    void shouldReturnAllAddedTasks(){
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration = Duration.ofMinutes(120);
        Task task1 = new Task("Имя задачи", "Описание задачи", startTime, duration);
        taskManager.addTask(task1);
        Task task2 = new Task("Имя задачи2", "Описание задачи2");
        taskManager.addTask(task2);
        Task task3 = new Task("Имя задачи3", "Описание задачи3", startTime.minusMonths(1), duration);
        taskManager.addTask(task3);

        assertEquals(3,taskManager.getTasks().size(),"Возвращает неверное количество задач");
        assertEquals(2,taskManager.getPrioritizedTasks().size(), "Возвращает неверное количество " +
                "задач, которые содержат время");
    }

    @Test
    void checkDeleteAllTasks(){
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration = Duration.ofMinutes(120);
        Task task1 = new Task("Имя задачи", "Описание задачи", startTime, duration);
        taskManager.addTask(task1);
        Task task2 = new Task("Имя задачи2", "Описание задачи2");
        taskManager.addTask(task2);
        Task task3 = new Task("Имя задачи3", "Описание задачи3", startTime.minusMonths(1), duration);
        taskManager.addTask(task3);
        taskManager.deleteAllTasks();

        assertNotNull(taskManager.getTasks(), "Возвращает null список задач после удаления");
        assertNotNull(taskManager.getPrioritizedTasks(),"Возвращает null список задач, которые содержат " +
                "время, после удаления");
        assertTrue(taskManager.getTasks().isEmpty(), "Возвращает False при пустом списке");
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(), "Возвращает False при пустом списке задач " +
                "со временем");
    }

    @Test
    void addEpicAndGetItById(){
        Epic epic = new Epic("Имя эпика'", "Описание эпика");
        taskManager.addEpic(epic);
        LocalDateTime startTime = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration = Duration.ofMinutes(120);
        Subtask subtask = new Subtask(epic.getId(), "Имя подзадачи","Описание подзадачи",
                Status.DONE, startTime,duration);
        taskManager.addSubtask(subtask);
        Task gettedEpic = taskManager.getEpicById(epic.getId());

        assertNotNull(gettedEpic, "Эпик не должен быть null после добавления и получения");
        assertEquals(epic, gettedEpic, "id эпиков не совпадает");
        assertEquals(epic.getName(), gettedEpic.getName(), "Имя эпика не совпадает");
        assertEquals(epic.getDescription(), gettedEpic.getDescription(), "Описание эпика не совпадает");
        assertEquals(epic.getStatus(), gettedEpic.getStatus(), "Статусы не совпадают");
        assertEquals(epic.getStartTime(), gettedEpic.getStartTime(), "Время старта не совпадает");
        assertEquals(epic.getDuration(), gettedEpic.getDuration(), "Продолжительность эпика не совпадает");
        assertEquals(epic.getEndTime(), gettedEpic.getEndTime(), "Время окончания не совпадает");
    }

    @Test
    void shouldAddEmptyEpicAndGetCorrectInitialState() {
        Epic newEpic = new Epic("Пустой Эпик", "Эпик без подзадач");
        taskManager.addEpic(newEpic);
        int epicId = newEpic.getId();
        Epic retrievedEpic = taskManager.getEpicById(epicId);

        assertNotNull(retrievedEpic);
        assertEquals(epicId, retrievedEpic.getId());
        assertEquals("Пустой Эпик", retrievedEpic.getName());
        assertEquals("Эпик без подзадач", retrievedEpic.getDescription());

        assertEquals(Status.NEW, retrievedEpic.getStatus(), "Статус нового эпика без подзадач должен быть " +
                "NEW");
        assertNull(retrievedEpic.getEpicStartTime(), "Время начала нового эпика без подзадач должно быть " +
                "null");
        assertNull(retrievedEpic.getEpicEndTime(), "Время окончания нового эпика без подзадач должно быть " +
                "null");
        assertEquals(Duration.ZERO, retrievedEpic.getEpicDuration(), "Продолжительность нового эпика без " +
                "подзадач должна быть ZERO");

        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertTrue(subtasks.isEmpty(), "У нового эпика не должно быть подзадач");
    }

    @Test
    void shouldUpdateEpicNameAndDescription() {
        Epic originalEpic = new Epic("Старое Имя", "Старое Описание");
        taskManager.addEpic(originalEpic);
        int epicId = originalEpic.getId();

        Subtask subtask1 = new Subtask(epicId, "Подзадача 1", "Для проверки времени и статуса",
                Status.IN_PROGRESS,
                LocalDateTime.now(), Duration.ofHours(1));
        taskManager.addSubtask(subtask1);

        Epic epicBeforeUpdate = taskManager.getEpicById(epicId);
        assertNotNull(epicBeforeUpdate);
        Status statusBeforeUpdate = epicBeforeUpdate.getStatus();
        LocalDateTime startTimeBeforeUpdate = epicBeforeUpdate.getEpicStartTime();
        LocalDateTime endTimeBeforeUpdate = epicBeforeUpdate.getEpicEndTime();
        Duration durationBeforeUpdate = epicBeforeUpdate.getEpicDuration();

        Epic epicWithUpdates = new Epic("Новое Имя", "Новое Описание");
        epicWithUpdates.setId(epicId);
        boolean isUpdated = taskManager.update(epicWithUpdates);
        Epic retrievedEpicAfterUpdate = taskManager.getEpicById(epicId);
        assertTrue(isUpdated, "Метод update должен вернуть true для существующего эпика");
        assertNotNull(retrievedEpicAfterUpdate, "Эпик не должен быть null после обновления");
        assertEquals(epicId, retrievedEpicAfterUpdate.getId(), "ID эпика не должен измениться");
        assertEquals("Новое Имя", retrievedEpicAfterUpdate.getName(), "Имя эпика должно было " +
                "обновиться");
        assertEquals("Новое Описание", retrievedEpicAfterUpdate.getDescription(), "Описание эпика " +
                "должно было обновиться");
        assertEquals(statusBeforeUpdate, retrievedEpicAfterUpdate.getStatus(), "Статус эпика не должен был " +
                "измениться, если подзадачи не трогали и update(Epic) его корректно пересчитывает");
        assertEquals(startTimeBeforeUpdate, retrievedEpicAfterUpdate.getEpicStartTime(), "Время начала " +
                "эпика не должно было измениться");
        assertEquals(endTimeBeforeUpdate, retrievedEpicAfterUpdate.getEpicEndTime(), "Время окончания эпика " +
                "не должно было измениться");
        assertEquals(durationBeforeUpdate, retrievedEpicAfterUpdate.getEpicDuration(), "Продолжительность " +
                "эпика не должна была измениться");
        assertEquals(1, taskManager.getEpicSubtasks(epicId).size(), "Количество подзадач не должно " +
                "было измениться");
    }

    @Test
    void shouldDeleteExistingEpicAndItsSubtasks() {
        Epic epic = new Epic("Эпик на удаление", "Описание");
        taskManager.addEpic(epic);
        int epicId = epic.getId();
        Subtask subtask1 = new Subtask(epicId, "Подзадача 1", "Описание 1", Status.NEW,
                LocalDateTime.now(), Duration.ofHours(1));
        taskManager.addSubtask(subtask1);
        int subtask1Id = subtask1.getId();
        Subtask subtask2 = new Subtask(epicId, "Подзадача 2", "Описание 2");
        taskManager.addSubtask(subtask2);
        int subtask2Id = subtask2.getId();
        assertNotNull(taskManager.getEpicById(epicId));
        assertNotNull(taskManager.getSubtaskById(subtask1Id));
        assertNotNull(taskManager.getSubtaskById(subtask2Id));
        assertEquals(2, taskManager.getEpicSubtasks(epicId).size());
        assertTrue(taskManager.getPrioritizedTasks().stream().anyMatch(t -> t.getId() == subtask1Id));
        boolean isDeleted = taskManager.deleteEpicById(epicId);
        assertTrue(isDeleted, "Удаление существующего эпика должно вернуть true");
        assertNull(taskManager.getEpicById(epicId), "Эпик должен быть удален");
        assertNull(taskManager.getSubtaskById(subtask1Id), "Подзадача 1 должна быть удалена вместе с эпиком");
        assertNull(taskManager.getSubtaskById(subtask2Id), "Подзадача 2 должна быть удалена вместе с эпиком");
        assertTrue(taskManager.getEpicSubtasks(epicId).isEmpty(), "Список подзадач удаленного эпика должен " +
                "быть пуст");
        final int finalSubtask1Id = subtask1Id;
        assertTrue(taskManager.getPrioritizedTasks().stream().noneMatch(t -> t.getId() == finalSubtask1Id),
                "Подзадача 1 должна быть удалена из prioritizedTasks");

    }
    @Test
    void shouldReturnFalseWhenDeletingNonExistentEpic() {
        int nonExistentEpicId = 9999;
        boolean isDeleted = taskManager.deleteEpicById(nonExistentEpicId);
        assertFalse(isDeleted, "Метод deleteEpicById должен вернуть false при попытке удалить эпик с " +
                "несуществующим ID");
        assertNull(taskManager.getEpicById(nonExistentEpicId), "Эпика с несуществующим ID не должно быть " +
                "в менеджере");
    }

    @Test
    void shouldReturnEmptyListWhenNoEpicsAdded() {
        List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Список эпиков не должен быть null");
        assertTrue(epics.isEmpty(), "Список эпиков должен быть пуст");
    }

    @Test
    void shouldReturnAllAddedEpics() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        taskManager.addEpic(epic1);
        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        taskManager.addEpic(epic2);
        List<Epic> actualEpics = taskManager.getEpics();
        assertNotNull(actualEpics);
        assertEquals(2, actualEpics.size(), "Неверное количество эпиков в списке");
        List<Integer> expectedEpicIds = List.of(epic1.getId(), epic2.getId());
        List<Integer> actualEpicIds = actualEpics.stream().map(Epic::getId).collect(Collectors.toList());
        assertTrue(actualEpicIds.containsAll(expectedEpicIds) && expectedEpicIds.containsAll(actualEpicIds),
                "Список эпиков не содержит все добавленные эпики или содержит лишние");
    }

    @Test
    void shouldDeleteAllEpicsAndClearSubtaskMap() {
        Epic epic1 = new Epic("Эпик 1", "Для удаления");
        taskManager.addEpic(epic1);
        Subtask sub1_epic1 = new Subtask(epic1.getId(), "Подзадача 1.1", "Описание", Status.NEW,
                LocalDateTime.now(), Duration.ofHours(1));
        taskManager.addSubtask(sub1_epic1);
        Epic epic2 = new Epic("Эпик 2", "Тоже для удаления");
        taskManager.addEpic(epic2);
        Subtask sub1_epic2 = new Subtask(epic2.getId(), "Подзадача 2.1", "Описание");
        taskManager.addSubtask(sub1_epic2);
        Task regularTask = new Task("Обычная задача", "Не должна удалиться");
        taskManager.addTask(regularTask);
        int regularTaskId = regularTask.getId();
        assertFalse(taskManager.getEpics().isEmpty(), "Список эпиков не должен быть пуст до удаления");
        assertFalse(taskManager.getSubtasks().isEmpty(), "Список подзадач не должен быть пуст до удаления");
        assertTrue(taskManager.getPrioritizedTasks().stream().anyMatch(t -> t.getId() == sub1_epic1.getId()),
                "Подзадача с временем должна быть в prioritizedTasks до удаления всех эпиков");
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пуст после deleteAllEpics");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пуст после " +
                "deleteAllEpics (т.к. subtaskMap очищается)");
        assertNotNull(taskManager.getTaskById(regularTaskId), "Обычная задача не должна была удалиться");
        assertEquals(1, taskManager.getTasks().size(), "Должна остаться одна обычная задача");
        final int finalSub1Epic1Id = sub1_epic1.getId();
        assertTrue(taskManager.getPrioritizedTasks().stream().anyMatch(t -> t.getId() == finalSub1Epic1Id),
                "ПОКА ЧТО: Подзадача sub1_epic1 остается в prioritizedTasks после deleteAllEpics (это может " +
                        "быть нежелательно)");
    }

    @Test
    void shouldThrowTaskOverlapExceptionWhenAddingOverlappingTask() {
        LocalDateTime startTime1 = LocalDateTime.of(2025, 5, 9, 17, 33);
        Duration duration1 = Duration.ofMinutes(120);
        Task task1 = new Task("Задача 1", "Описание 1", startTime1, duration1);
        taskManager.addTask(task1);
        int initialTaskCount = taskManager.getTasks().size();
        LocalDateTime startTime2 = startTime1.minusHours(1);
        Duration duration2 = Duration.ofMinutes(120);
        Task task2overlapping = new Task("Задача 2 (пересекается)", "Описание 2", startTime2,
                duration2);
        TaskOverlapException thrownException = assertThrows(TaskOverlapException.class, () -> {
            taskManager.addTask(task2overlapping);
        }, "Должно быть выброшено TaskOverlapException при добавлении пересекающейся задачи");
        assertEquals(initialTaskCount, taskManager.getTasks().size(),
                "Количество задач не должно было измениться после попытки добавить пересекающуюся задачу");
    }
}

