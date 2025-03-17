import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private ArrayList<Task> historyList = new ArrayList<>();

    @Override
    public ArrayList <Task> getHistory(){
        return historyList;
    }
    @Override
    public void add(Task task) {
        historyList.add(task);
        if (historyList.size() > 10) {
            historyList.remove(0);
        }
    }
}
