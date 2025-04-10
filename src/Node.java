public class Node {
    Node prev;
    Node next;
    Task task;

    public Node(Task task) {
        this.task = task;
        this.prev = null;
        this.next = null;
    }

    public Node(Node prev, Task task, Node next) {
        this.prev = prev;
        this.task = task;
        this.next = next;
    }
}
