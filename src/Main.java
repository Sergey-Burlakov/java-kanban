public class Main {

    public static void main(String[] args) {
        // InMemoryTaskManager kanban = new InMemoryTaskManager();
        TaskManager kanban = Managers.getDefault();
        Task task1 = new Task("Магазин", "Пойти в магазин по адресу улица Пушкина");
        kanban.addTask(task1);
        Task task2 = new Task("Спорт", "побегать на беговой дорожке");
        kanban.addTask(task2);

        System.out.println("\u001b[36;1m"+ "1. Проверка добавления обычных задач" +"\u001b[0m");
        System.out.println(task1.toString());
        System.out.println(task2.toString());
        System.out.println();

        Epic epic1 = new Epic("Финальное задание №4","господи, успеть все бы сдать");
        kanban.addEpic(epic1);
        Subtask subtask1 = new Subtask(epic1.getId(),"Дописать тесты","Я очень надеюсь, что у меня " +
                "программа написана плюс минус правильно");
        kanban.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic1.getId(),"Сдать проект","учесть все замечения ревьюера");
        kanban.addSubtask(subtask2);

        Epic epic2 = new Epic("Эпик без описания");
        kanban.addEpic(epic2);
        Subtask subtask3 = new Subtask(epic2.getId(), "Подзадача");
        kanban.addSubtask(subtask3);

        System.out.println("\u001b[36;1m"+ "2. Проверка эпиков и подзадач в них" +"\u001b[0m");
        System.out.println(epic1.toString());
        System.out.println(subtask1.toString());
        System.out.println(subtask2.toString());
        System.out.println();
        System.out.println(epic2.toString());
        System.out.println(subtask3.toString());
        System.out.println();


        System.out.println("\u001b[36;1m"+ "3. Обновления статусов" +"\u001b[0m");
        subtask3.setStatus(Status.DONE);
       // kanban.update(subtask3);
        kanban.update(epic2);
        System.out.println(epic2.toString());
        System.out.println(subtask3.toString());
        System.out.println();

        System.out.println("\u001b[36;1m"+ "4. Удаляем епик и подзадачу" +"\u001b[0m");
        kanban.deleteSubtaskById(subtask3.getId());
        kanban.deleteEpicById(epic2.getId());
        System.out.println(kanban.toString());
    }

}
