import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final HashMap<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void add(Task task) {
        if (historyMap.containsKey(task.getId())) {
            Node nodeToRemove = historyMap.get(task.getId());
            removeNode(nodeToRemove);
        }
        linkLast(task);
        historyMap.put(task.getId(), tail);
    }

    @Override
    public void remove(int id) {
        if (!historyMap.containsKey(id)) {
            return;
        }
        removeNode(historyMap.get(id));
        historyMap.remove(id);
    }

    private void removeNode(Node node) {
        if (node == this.tail && node == this.head) {
            this.head = null;
            this.tail = null;
            // Удаляем голову
        } else if (node == this.head) {
            this.head = node.next;
            this.head.prev = null;
            // Удаляем хвост
        } else if (node == this.tail) {
            this.tail = node.prev;
            this.tail.next = null;
            // Удаляем узел из середины
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasksList = new ArrayList<>();
        Node currentNode = this.head;
        while (currentNode != null) {
            tasksList.add(currentNode.task); // Сразу добавляем задачу из узла
            currentNode = currentNode.next;
        }
        return tasksList;
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task);
        if (this.head == null) {
            head = newNode;
            tail = newNode;
        } else {
            Node oldTail = this.tail;
            oldTail.next = newNode;
            newNode.prev = oldTail;
            this.tail = newNode;
        }
    }
}
